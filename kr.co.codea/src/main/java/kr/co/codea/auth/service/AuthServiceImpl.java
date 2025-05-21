package kr.co.codea.auth.service;

import kr.co.codea.auth.config.JwtProvider;
import kr.co.codea.auth.dto.LoginRequestDto;
import kr.co.codea.auth.dto.RefreshToken;
import kr.co.codea.auth.dto.TokenDto;
import kr.co.codea.auth.dto.UserDetailsDto;
import kr.co.codea.auth.mapper.RefreshTokenMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Slf4j 로거 추가
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails; // UserDetails 인터페이스는 계속 사용
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Slf4j // Slf4j 로거 추가
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final RefreshTokenMapper refreshTokenMapper;
    private final UserDetailsServiceImpl userDetailsServiceImpl; // UserDetails 로드용

    @Transactional
    @Override
    public TokenDto login(LoginRequestDto loginRequestDto) {
        log.info("AuthServiceImpl: Login attempt for loginId: {}", loginRequestDto.getLoginId());

        // 1. Login ID/PW 를 기반으로 AuthenticationToken 생성
        // 여기서 loginRequestDto.getLoginId()는 EMP_USER_ID를 나타냅니다.
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginRequestDto.getLoginId(), loginRequestDto.getPassword());

        // 2. 실제 검증 (사용자 비밀번호 체크)
        // authenticate() 메서드가 실행될 때 UserDetailsServiceImpl 에서 만든 loadUserByUsername() 실행됨
        // UserDetailsServiceImpl은 UserDetailsDto (변경된 필드명 empUserId, empPw, empStatus 포함)를 반환합니다.
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        log.info("AuthServiceImpl: Authentication successful for principal: {}", authentication.getName());


        // 3. SecurityContext에 Authentication 객체 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 4. 인증 정보를 기반으로 JWT 토큰 생성
        // JwtProvider.createAccessToken/createRefreshToken은 Authentication 객체를 받으며,
        // 내부적으로 authentication.getPrincipal() (UserDetailsDto)과 authentication.getName() (UserDetailsDto.getUsername() -> empUserId)을 사용합니다.
        // UserDetailsDto의 getEmpId(), getUsername() 등이 올바르게 값을 반환하면 문제 없습니다.
        String accessToken = jwtProvider.createAccessToken(authentication);
        String refreshTokenValue = jwtProvider.createRefreshToken(authentication);
        log.debug("AuthServiceImpl: AccessToken created, RefreshToken created.");

        // 5. RefreshToken DB에 저장/업데이트
        // authentication.getPrincipal()은 UserDetailsServiceImpl이 반환한 UserDetailsDto 인스턴스입니다.
        UserDetailsDto userDetailsPrincipal = (UserDetailsDto) authentication.getPrincipal();

        // UserDetailsDto의 getEmpId() 메소드가 Long 타입의 EMP_ID를 반환해야 합니다.
        Long actualEmpId = userDetailsPrincipal.getEmpId();
        if (actualEmpId == null) {
            log.error("AuthServiceImpl: EMP_ID is null in UserDetailsDto for authenticated user: {}", userDetailsPrincipal.getEmpUserId());
            throw new IllegalStateException("Authenticated user has no EMP_ID. Principal: " + userDetailsPrincipal);
        }
        log.info("AuthServiceImpl: empId for RefreshToken: {}", actualEmpId);


        RefreshToken refreshToken = RefreshToken.builder()
                .empId(actualEmpId)
                .token(refreshTokenValue)
                .expiration(new Date(System.currentTimeMillis() + jwtProvider.getRefreshTokenExpirationMillis()))
                .build();

        refreshTokenMapper.findByEmpId(actualEmpId)
                .ifPresentOrElse(
                        existingToken -> {
                            log.info("AuthServiceImpl: Updating existing RefreshToken for empId: {}", actualEmpId);
                            existingToken.updateTokenInfo(refreshTokenValue, refreshToken.getExpiration());
                            refreshTokenMapper.update(existingToken);
                        },
                        () -> {
                            log.info("AuthServiceImpl: Inserting new RefreshToken for empId: {}", actualEmpId);
                            refreshTokenMapper.insert(refreshToken);
                        }
                );

        return new TokenDto(accessToken, refreshTokenValue);
    }

    @Transactional
    @Override
    public void logout(Long empId) {
        log.info("AuthServiceImpl: Logout request for empId: {}", empId);
        refreshTokenMapper.deleteByEmpId(empId);
        SecurityContextHolder.clearContext();
        log.info("AuthServiceImpl: RefreshToken deleted from DB and SecurityContext cleared for empId: {}", empId);
    }

    @Transactional
    @Override
    public TokenDto reissueToken(String refreshTokenValue) {
        log.info("AuthServiceImpl: Token reissue attempt with RefreshToken (first 10 chars): {}",
                (refreshTokenValue != null && refreshTokenValue.length() > 10) ? refreshTokenValue.substring(0, 10) + "..." : refreshTokenValue);

        // 1. Refresh Token 유효성 검증
        if (!jwtProvider.validateToken(refreshTokenValue)) {
            log.warn("AuthServiceImpl: Invalid RefreshToken provided for reissue.");
            throw new RuntimeException("Invalid Refresh Token"); // TODO: Custom Exception 처리
        }

        // 2. DB에서 Refresh Token 조회 및 서버 저장본과 일치 여부 확인
        RefreshToken storedRefreshToken = refreshTokenMapper.findByToken(refreshTokenValue)
                .orElseThrow(() -> {
                    log.warn("AuthServiceImpl: RefreshToken not found in DB for reissue.");
                    return new RuntimeException("Refresh Token not found in DB"); // TODO: Custom Exception
                });

        // 3. Refresh Token 만료 시간 검증
        if (storedRefreshToken.getExpiration().before(new Date())) {
            log.warn("AuthServiceImpl: Expired RefreshToken used for reissue. Deleting token for empId: {}", storedRefreshToken.getEmpId());
            refreshTokenMapper.deleteByToken(refreshTokenValue); // 만료된 토큰은 삭제
            throw new RuntimeException("Expired Refresh Token. Please login again."); // TODO: Custom Exception
        }

        // 4. Refresh Token에서 사용자 정보 가져와서 새로운 Authentication 객체 생성
        // JwtProvider.getUserIdFromToken은 토큰의 subject (EMP_USER_ID)를 반환합니다.
        String empUserIdFromRefreshToken = jwtProvider.getUserIdFromToken(refreshTokenValue);
        if (empUserIdFromRefreshToken == null) {
            log.error("AuthServiceImpl: Could not extract empUserId from refresh token for reissue.");
            throw new RuntimeException("Invalid refresh token content.");
        }

        // UserDetailsServiceImpl.loadUserByUsername을 호출하여 UserDetails 객체를 가져옵니다.
        // (토큰 재발급 시 empId로 조회하는 loadUserByEmpId를 사용할 수도 있습니다.
        //  만약 RefreshToken의 subject가 EMP_ID(숫자)라면 Long.parseLong() 후 loadUserByEmpId 사용)
        // 현재 JwtProvider.createRefreshToken은 authentication.getName() (EMP_USER_ID)를 subject로 사용하므로,
        // loadUserByUsername(empUserIdFromRefreshToken)을 사용하는 것이 맞습니다.
        UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(empUserIdFromRefreshToken);
        log.info("AuthServiceImpl: UserDetails loaded for reissue: {}", userDetails.getUsername());


        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, "", userDetails.getAuthorities());

        // 5. 새로운 Access Token 발급
        String newAccessToken = jwtProvider.createAccessToken(authentication);
        log.info("AuthServiceImpl: New AccessToken created for user: {}", userDetails.getUsername());


        // (선택) Refresh Token Rotation: 재발급 시 Refresh Token도 새로 발급하고 기존 것은 삭제/무효화
        // String newRefreshTokenValue = jwtProvider.createRefreshToken(authentication);
        // storedRefreshToken.updateTokenInfo(newRefreshTokenValue, new Date(System.currentTimeMillis() + jwtProvider.getRefreshTokenExpirationMillis()));
        // refreshTokenMapper.update(storedRefreshToken);
        // log.info("AuthServiceImpl: RefreshToken rotated for user: {}", userDetails.getUsername());
        // return new TokenDto(newAccessToken, newRefreshTokenValue);

        return new TokenDto(newAccessToken, refreshTokenValue); // 기존 Refresh Token 사용
    }
}