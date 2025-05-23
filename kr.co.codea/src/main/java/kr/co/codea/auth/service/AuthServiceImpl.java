package kr.co.codea.auth.service;

import kr.co.codea.auth.config.JwtProvider;
import kr.co.codea.auth.dto.LoginRequestDto;
import kr.co.codea.auth.dto.RefreshToken;
import kr.co.codea.auth.dto.TokenDto;
import kr.co.codea.auth.dto.UserDetailsDto;
import kr.co.codea.auth.mapper.RefreshTokenMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final RefreshTokenMapper refreshTokenMapper;
    private final UserDetailsServiceImpl userDetailsServiceImpl;

    /**
     * 로그인 처리: 인증 → 토큰 발급 → RefreshToken 저장
     */
    @Transactional
    @Override
    public TokenDto login(LoginRequestDto loginRequestDto) {
        // log.info("Login attempt for loginId: {}", loginRequestDto.getLoginId());

        // 로그인 ID/PW 기반 AuthenticationToken 생성
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginRequestDto.getLoginId(), loginRequestDto.getPassword());

        // 실제 인증 처리
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        // log.info("Authentication successful for principal: {}", authentication.getName());

        // 인증 객체를 SecurityContext에 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // JWT 토큰 생성
        String accessToken = jwtProvider.createAccessToken(authentication);
        String refreshTokenValue = jwtProvider.createRefreshToken(authentication);
        // log.debug("AccessToken created, RefreshToken created.");

        // 인증된 사용자 정보에서 empId 추출
        UserDetailsDto userDetailsPrincipal = (UserDetailsDto) authentication.getPrincipal();
        Long actualEmpId = userDetailsPrincipal.getEmpId();
        if (actualEmpId == null) {
            // log.error("EMP_ID is null for authenticated user: {}", userDetailsPrincipal.getEmpUserId());
            throw new IllegalStateException("Authenticated user has no EMP_ID.");
        }
        // log.info("empId for RefreshToken: {}", actualEmpId);

        // RefreshToken 객체 구성
        RefreshToken refreshToken = RefreshToken.builder()
                .empId(actualEmpId)
                .token(refreshTokenValue)
                .expiration(new Date(System.currentTimeMillis() + jwtProvider.getRefreshTokenExpirationMillis()))
                .build();

        // DB에 기존 토큰이 있으면 업데이트, 없으면 insert
        refreshTokenMapper.findByEmpId(actualEmpId)
                .ifPresentOrElse(
                        existingToken -> {
                            // log.info("Updating existing RefreshToken for empId: {}", actualEmpId);
                            existingToken.updateTokenInfo(refreshTokenValue, refreshToken.getExpiration());
                            refreshTokenMapper.update(existingToken);
                        },
                        () -> {
                            // log.info("Inserting new RefreshToken for empId: {}", actualEmpId);
                            refreshTokenMapper.insert(refreshToken);
                        }
                );

        return new TokenDto(accessToken, refreshTokenValue);
    }

    /**
     * 로그아웃 처리: RefreshToken 삭제 및 SecurityContext 초기화
     */
    @Transactional
    @Override
    public void logout(Long empId) {
        // log.info("Logout request for empId: {}", empId);
        refreshTokenMapper.deleteByEmpId(empId);
        SecurityContextHolder.clearContext();
        // log.info("RefreshToken deleted and SecurityContext cleared for empId: {}", empId);
    }

    /**
     * RefreshToken을 이용한 AccessToken 재발급 처리
     */
    @Transactional
    @Override
    public TokenDto reissueToken(String refreshTokenValue) {
        // log.info("Token reissue attempt with RefreshToken: {}", tokenAbbreviated(refreshTokenValue));

        // Refresh Token 유효성 검사
        if (!jwtProvider.validateToken(refreshTokenValue)) {
            // log.warn("Invalid RefreshToken for reissue.");
            throw new RuntimeException("Invalid Refresh Token");
        }

        // DB에 저장된 RefreshToken 조회
        RefreshToken storedRefreshToken = refreshTokenMapper.findByToken(refreshTokenValue)
                .orElseThrow(() -> {
                    // log.warn("RefreshToken not found in DB.");
                    return new RuntimeException("Refresh Token not found in DB");
                });

        // 만료 여부 검사
        if (storedRefreshToken.getExpiration().before(new Date())) {
            // log.warn("Expired RefreshToken. Deleting...");
            refreshTokenMapper.deleteByToken(refreshTokenValue);
            throw new RuntimeException("Expired Refresh Token. Please login again.");
        }

        // 토큰에서 사용자 ID 추출
        String empUserIdFromRefreshToken = jwtProvider.getUserIdFromToken(refreshTokenValue);
        if (empUserIdFromRefreshToken == null) {
            // log.error("Could not extract empUserId from token.");
            throw new RuntimeException("Invalid refresh token content.");
        }

        // 사용자 정보 로드
        UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(empUserIdFromRefreshToken);
        // log.info("UserDetails loaded for reissue: {}", userDetails.getUsername());

        // 새로운 Authentication 생성
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, "", userDetails.getAuthorities());

        // Access Token 재발급
        String newAccessToken = jwtProvider.createAccessToken(authentication);
        // log.info("New AccessToken created for user: {}", userDetails.getUsername());

        // (선택) Refresh Token 재발급 및 Rotation 처리 가능

        return new TokenDto(newAccessToken, refreshTokenValue);
    }
}
