package kr.co.codea.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.codea.auth.config.JwtProvider;
import kr.co.codea.auth.dto.LoginRequestDto;
import kr.co.codea.auth.dto.TokenDto;
import kr.co.codea.auth.dto.UserDetailsDto;
import kr.co.codea.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtProvider jwtProvider;

    /**
     * 로그인 요청 처리
     * - Access Token: HttpOnly 쿠키로 설정
     * - Refresh Token: 응답 본문에 포함
     */
    @PostMapping("/login")
    public ResponseEntity<TokenDto> login(@RequestBody LoginRequestDto loginRequestDto, HttpServletResponse response) {
        TokenDto tokenDto = authService.login(loginRequestDto);

        // Access Token을 HttpOnly + Secure + SameSite=Strict 쿠키로 설정
        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", tokenDto.getAccessToken())
                .httpOnly(true)
                .secure(true) // 운영 환경에서 필수
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofMillis(jwtProvider.getAccessTokenExpirationMillis()))
                .build();

        response.addHeader("Set-Cookie", accessTokenCookie.toString());

        // Refresh Token은 응답 본문에만 포함
        return ResponseEntity.ok(new TokenDto(null, tokenDto.getRefreshToken()));
    }

    /**
     * 로그아웃 처리
     * - DB에서 Refresh Token 제거
     * - Access Token 쿠키 삭제
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse httpResponse, @AuthenticationPrincipal UserDetailsDto userDetailsDto) {
        Long empIdToLogout = null;

        if (userDetailsDto != null && userDetailsDto.getEmpId() != null) {
            empIdToLogout = userDetailsDto.getEmpId();
        } else {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetailsDto principal) {
                empIdToLogout = principal.getEmpId();
            }
        }

        // Refresh Token 삭제
        if (empIdToLogout != null) {
            authService.logout(empIdToLogout);
        }

        // Access Token 쿠키 제거 (즉시 만료)
        ResponseCookie deleteCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();

        httpResponse.addHeader("Set-Cookie", deleteCookie.toString());

        SecurityContextHolder.clearContext(); // 인증 정보 제거
        return ResponseEntity.ok().build();
    }

    /**
     * Access Token 재발급 요청 처리
     * - 요청 본문에 포함된 Refresh Token 기반으로 재발급
     * - 새 Access Token은 HttpOnly 쿠키로 설정
     */
    @PostMapping("/reissue")
    public ResponseEntity<TokenDto> reissue(@RequestBody TokenDto requestTokenDto, HttpServletResponse response) {
        TokenDto newTokens = authService.reissueToken(requestTokenDto.getRefreshToken());

        // 새 Access Token을 HttpOnly + Secure + SameSite=Strict 쿠키로 설정
        ResponseCookie newAccessTokenCookie = ResponseCookie.from("accessToken", newTokens.getAccessToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofMillis(jwtProvider.getAccessTokenExpirationMillis()))
                .build();

        response.addHeader("Set-Cookie", newAccessTokenCookie.toString());

        // Refresh Token은 응답 본문 유지
        return ResponseEntity.ok(new TokenDto(null, requestTokenDto.getRefreshToken()));
    }
}
