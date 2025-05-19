package kr.co.codea.auth.controller;

import kr.co.codea.auth.dto.LoginRequestDto;
import kr.co.codea.auth.dto.TokenDto;
import kr.co.codea.auth.dto.UserDetailsDto;
import kr.co.codea.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*; // @RequestHeader 사용 시

@RestController
@RequestMapping("/auth") // 사용자가 요청한 기본 경로
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<TokenDto> login(@RequestBody LoginRequestDto loginRequestDto) {
        TokenDto tokenDto = authService.login(loginRequestDto);
        return ResponseEntity.ok(tokenDto);
    }

    // 로그아웃은 현재 인증된 사용자를 기준으로 처리
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal UserDetailsDto userDetailsDto) {
        // @AuthenticationPrincipal을 사용하면 현재 인증된 사용자의 UserDetails 객체를 직접 받을 수 있습니다.
        // 단, UserDetailsDto의 getUsername()이 Long 타입의 empId를 문자열로 반환해야 합니다.
        if (userDetailsDto != null) {
            Long empId = Long.parseLong(userDetailsDto.getUsername());
            authService.logout(empId);
            return ResponseEntity.ok().build();
        }
        // 또는 SecurityContextHolder에서 직접 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsDto) {
            UserDetailsDto principal = (UserDetailsDto) authentication.getPrincipal();
            Long empId = Long.parseLong(principal.getUsername());
            authService.logout(empId);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(401).build(); // 인증 정보 없을 시
    }

    // Refresh Token을 Request Body로 받는 예시
    // 프론트엔드와 협의하여 헤더(X-Refresh-Token) 또는 HttpOnly 쿠키로 받을 수도 있습니다.
    @PostMapping("/reissue")
    public ResponseEntity<TokenDto> reissue(@RequestBody TokenDto requestTokenDto) { // 간단히 TokenDto 재활용 또는 ReissueRequestDto 생성
        TokenDto tokenDto = authService.reissueToken(requestTokenDto.getRefreshToken());
        return ResponseEntity.ok(tokenDto);
    }
}