package kr.co.codea.auth.service;

import kr.co.codea.auth.config.JwtProvider;
import kr.co.codea.auth.dto.LoginRequestDto;
import kr.co.codea.auth.dto.RefreshToken;
import kr.co.codea.auth.dto.TokenDto;
import kr.co.codea.auth.dto.UserDetailsDto;
import kr.co.codea.auth.mapper.RefreshTokenMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

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
        // 1. Login ID/PW 를 기반으로 AuthenticationToken 생성
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginRequestDto.getLoginId(), loginRequestDto.getPassword());

        // 2. 실제 검증 (사용자 비밀번호 체크)
        // authenticate() 메서드가 실행될 때 UserDetailsServiceImpl 에서 만든 loadUserByUsername() 실행됨
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        // 3. SecurityContext에 Authentication 객체 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 4. 인증 정보를 기반으로 JWT 토큰 생성
        String accessToken = jwtProvider.createAccessToken(authentication);
        String refreshTokenValue = jwtProvider.createRefreshToken(authentication);

        // 5. RefreshToken DB에 저장/업데이트
        UserDetailsDto userDetailsPrincipal = (UserDetailsDto) authentication.getPrincipal();
        // userDetailsPrincipal.getUsername()은 EMP_ID를 문자열로 반환해야 함
        Long actualEmpId = userDetailsPrincipal.getEmpId(); // UserDetailsDto에서 숫자 EMP_ID를 직접 가져옴

     // 이제 actualEmpId (숫자형 EMP_ID)를 RefreshToken 생성에 사용
     RefreshToken refreshToken = RefreshToken.builder()
             .empId(actualEmpId) // 수정된 부분: 실제 숫자 empId 사용
             .token(refreshTokenValue)
             .expiration(new Date(System.currentTimeMillis() + jwtProvider.getRefreshTokenExpirationMillis()))
             .build();

     refreshTokenMapper.findByEmpId(actualEmpId) // 수정된 부분: 실제 숫자 empId 사용
             .ifPresentOrElse(
                     existingToken -> {
                         existingToken.updateTokenInfo(refreshTokenValue, refreshToken.getExpiration());
                         refreshTokenMapper.update(existingToken);
                     },
                     () -> refreshTokenMapper.insert(refreshToken)
             );

        return new TokenDto(accessToken, refreshTokenValue);
    }

    @Transactional
    @Override
    public void logout(Long empId) {
        refreshTokenMapper.deleteByEmpId(empId);
        SecurityContextHolder.clearContext(); // 현재 스레드의 SecurityContext 정보 삭제
    }

    @Transactional
    @Override
    public TokenDto reissueToken(String refreshTokenValue) {
        // 1. Refresh Token 유효성 검증
        if (!jwtProvider.validateToken(refreshTokenValue)) {
            throw new RuntimeException("Invalid Refresh Token"); // TODO: Custom Exception 처리
        }

        // 2. DB에서 Refresh Token 조회 및 서버 저장본과 일치 여부 확인
        RefreshToken storedRefreshToken = refreshTokenMapper.findByToken(refreshTokenValue)
                .orElseThrow(() -> new RuntimeException("Refresh Token not found in DB")); // TODO: Custom Exception

        // 3. Refresh Token 만료 시간 검증
        if (storedRefreshToken.getExpiration().before(new Date())) {
            refreshTokenMapper.deleteByToken(refreshTokenValue); // 만료된 토큰은 삭제
            throw new RuntimeException("Expired Refresh Token. Please login again."); // TODO: Custom Exception
        }

        // 4. Refresh Token에서 사용자 정보 가져와서 새로운 Authentication 객체 생성
        //    (DB에서 사용자 정보를 다시 조회하는 것이 더 안전할 수 있습니다.)
        String empIdStr = jwtProvider.getUserIdFromToken(refreshTokenValue);
        UserDetails userDetails = userDetailsServiceImpl.loadUserByEmpId(Long.parseLong(empIdStr));
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, "", userDetails.getAuthorities());

        // 5. 새로운 Access Token 발급
        String newAccessToken = jwtProvider.createAccessToken(authentication);

        // (선택) Refresh Token Rotation: 재발급 시 Refresh Token도 새로 발급하고 기존 것은 삭제/무효화
        // String newRefreshTokenValue = jwtProvider.createRefreshToken(authentication);
        // storedRefreshToken.updateTokenInfo(newRefreshTokenValue, new Date(System.currentTimeMillis() + jwtProvider.getRefreshTokenExpirationMillis()));
        // refreshTokenMapper.update(storedRefreshToken);
        // return new TokenDto(newAccessToken, newRefreshTokenValue);

        return new TokenDto(newAccessToken, refreshTokenValue); // 기존 Refresh Token 사용
    }
}