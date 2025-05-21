package kr.co.codea.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.Cookie; // jakarta.servlet.http.Cookie 임포트
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse; // HttpServletResponse 임포트
import kr.co.codea.auth.config.JwtProvider; // JwtProvider 임포트 (만료 시간 등 가져오기 위해)
import kr.co.codea.auth.dto.LoginRequestDto;
import kr.co.codea.auth.dto.TokenDto;
// UserDetailsDto, Authentication, @AuthenticationPrincipal, SecurityContextHolder 등은 logout, reissue에서 계속 사용
import kr.co.codea.auth.dto.UserDetailsDto;
import kr.co.codea.auth.service.AuthService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtProvider jwtProvider; // JwtProvider 주입 (토큰 만료 시간 참조용)

    @PostMapping("/login")
    public ResponseEntity<TokenDto> login(@RequestBody LoginRequestDto loginRequestDto, HttpServletResponse response) {
        TokenDto tokenDto = authService.login(loginRequestDto);

        // 1. Access Token을 HttpOnly 쿠키로 설정
        Cookie accessTokenCookie = new Cookie("accessToken", tokenDto.getAccessToken());
        accessTokenCookie.setHttpOnly(true); // JavaScript에서 접근 불가
        accessTokenCookie.setPath("/");      // 애플리케이션 전체에서 유효
        // accessTokenCookie.setSecure(true); // HTTPS 환경에서만 전송 (배포 시 활성화 권장)
        accessTokenCookie.setMaxAge((int) (jwtProvider.getAccessTokenExpirationMillis() / 1000)); // 만료 시간 (초 단위)

        response.addCookie(accessTokenCookie);
        System.out.println("AuthController: Access Token 쿠키 설정 완료. 이름: accessToken, HttpOnly: true");

        // 2. 응답 본문에는 Refresh Token만 포함 (또는 아예 다른 성공 메시지)
        // 클라이언트 JavaScript는 이 Refresh Token을 localStorage 등에 저장하여 토큰 재발급에 사용
        return ResponseEntity.ok(new TokenDto(null, tokenDto.getRefreshToken()));
        // 또는: return ResponseEntity.ok().build(); // 본문 없이 성공 응답만
    }

    // ... logout, reissue 메소드는 이후 단계에서 쿠키 처리 방식에 맞춰 수정 예정 ...
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse httpResponse, @AuthenticationPrincipal UserDetailsDto userDetailsDto) { // HttpServletRequest 추가 (쿠키 정보 활용 가능성)
        Long empIdToLogout = null;
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

        if (empIdToLogout != null) {
            authService.logout(empIdToLogout); // DB에서 Refresh Token 삭제
            System.out.println("AuthController: DB에서 Refresh Token 삭제 완료 for empId: " + empIdToLogout);
        }

        // Access Token 쿠키 만료/삭제
        Cookie accessTokenCookie = new Cookie("accessToken", null); // 값을 null로
        accessTokenCookie.setMaxAge(0); // 만료 시간을 0으로 설정하여 즉시 만료
        accessTokenCookie.setPath("/");
        // accessTokenCookie.setHttpOnly(true); // 설정했던 속성들 동일하게 적용
        // accessTokenCookie.setSecure(true);  // 설정했던 속성들 동일하게 적용
        httpResponse.addCookie(accessTokenCookie);
        System.out.println("AuthController: Access Token 쿠키 만료 처리 완료.");

        // Refresh Token이 localStorage에 있다면 클라이언트 측에서 삭제해야 함
        // 만약 Refresh Token도 쿠키로 관리했다면 여기서 함께 만료 처리

        SecurityContextHolder.clearContext();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reissue")
    public ResponseEntity<TokenDto> reissue(@RequestBody TokenDto requestTokenDto, HttpServletResponse response) {
        // 현재 requestTokenDto.getRefreshToken()은 클라이언트의 localStorage 등에서 가져온 Refresh Token을 사용
        TokenDto newTokens = authService.reissueToken(requestTokenDto.getRefreshToken());

        // 새로 발급된 Access Token을 HttpOnly 쿠키로 설정
        Cookie newAccessTokenCookie = new Cookie("accessToken", newTokens.getAccessToken());
        newAccessTokenCookie.setHttpOnly(true);
        newAccessTokenCookie.setPath("/");
        newAccessTokenCookie.setMaxAge((int) (jwtProvider.getAccessTokenExpirationMillis() / 1000));
        // newAccessTokenCookie.setSecure(true); // 배포 시

        response.addCookie(newAccessTokenCookie);
        System.out.println("AuthController: 새 Access Token 쿠키 설정 완료 (재발급).");

        // 새 Refresh Token이 발급되었다면 (정책에 따라), 그것도 클라이언트에 전달해야 함
        // 현재 AuthServiceImpl은 기존 Refresh Token을 반환하므로, 여기서는 새 Access Token만 쿠키로 설정
        // 만약 AuthServiceImpl이 새 Refresh Token도 반환한다면:
        // return ResponseEntity.ok(new TokenDto(null, newTokens.getRefreshToken()));
        return ResponseEntity.ok(new TokenDto(null, requestTokenDto.getRefreshToken())); // 기존 Refresh Token 유지 시
    }
}