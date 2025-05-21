package kr.co.codea.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.codea.auth.config.JwtProvider;
import kr.co.codea.auth.dto.LoginRequestDto;
import kr.co.codea.auth.dto.TokenDto;
import kr.co.codea.auth.dto.UserDetailsDto;
import kr.co.codea.auth.service.AuthService;
import lombok.RequiredArgsConstructor;

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

        // Access Token을 HttpOnly 쿠키에 저장
        Cookie accessTokenCookie = new Cookie("accessToken", tokenDto.getAccessToken());
        accessTokenCookie.setHttpOnly(true); // JavaScript 접근 불가
        accessTokenCookie.setPath("/");      // 전체 경로에서 유효
        // accessTokenCookie.setSecure(true); // HTTPS 환경에서만 전송 (배포 시 활성화)
        accessTokenCookie.setMaxAge((int) (jwtProvider.getAccessTokenExpirationMillis() / 1000));

        response.addCookie(accessTokenCookie);

        // System.out.println("AuthController: Access Token 쿠키 설정 완료. 이름: accessToken, HttpOnly: true");

        // 응답 본문에는 Refresh Token만 전달
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

        // 인증된 사용자의 empId 가져오기 (직접 주입 또는 SecurityContext에서 추출)
        if (userDetailsDto != null && userDetailsDto.getEmpId() != null) {
            empIdToLogout = userDetailsDto.getEmpId();
        } else {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetailsDto) {
                UserDetailsDto principal = (UserDetailsDto) authentication.getPrincipal();
                if (principal.getEmpId() != null) {
                    empIdToLogout = principal.getEmpId();
                }
            }
        }

        // Refresh Token DB에서 삭제
        if (empIdToLogout != null) {
            authService.logout(empIdToLogout);
            // System.out.println("AuthController: DB에서 Refresh Token 삭제 완료 for empId: " + empIdToLogout);
        }

        // Access Token 쿠키 제거 (클라이언트에서 즉시 삭제됨)
        Cookie accessTokenCookie = new Cookie("accessToken", null);
        accessTokenCookie.setMaxAge(0); // 만료 즉시
        accessTokenCookie.setPath("/");
        // accessTokenCookie.setHttpOnly(true);
        // accessTokenCookie.setSecure(true);
        httpResponse.addCookie(accessTokenCookie);

        // System.out.println("AuthController: Access Token 쿠키 만료 처리 완료.");

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

        // 새 Access Token을 쿠키에 설정
        Cookie newAccessTokenCookie = new Cookie("accessToken", newTokens.getAccessToken());
        newAccessTokenCookie.setHttpOnly(true);
        newAccessTokenCookie.setPath("/");
        newAccessTokenCookie.setMaxAge((int) (jwtProvider.getAccessTokenExpirationMillis() / 1000));
        // newAccessTokenCookie.setSecure(true); // 배포 환경에서는 활성화

        response.addCookie(newAccessTokenCookie);

        // System.out.println("AuthController: 새 Access Token 쿠키 설정 완료 (재발급).");

        // 현재 정책: Refresh Token은 그대로 유지
        return ResponseEntity.ok(new TokenDto(null, requestTokenDto.getRefreshToken()));
    }
}
