package kr.co.codea.auth.controller;

import kr.co.codea.auth.dto.LoginRequestDto;
import kr.co.codea.auth.dto.TokenDto;
import kr.co.codea.auth.dto.UserDetailsDto;
import kr.co.codea.auth.service.AuthService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
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
    	if (userDetailsDto != null && userDetailsDto.getEmpId() != null) {
            // userDetailsDto.getEmpId()를 호출하면 Long 타입의 숫자 EMP_ID가 직접 반환됩니다.
            authService.logout(userDetailsDto.getEmpId());
            return ResponseEntity.ok().build();
        }
    	// 2. @AuthenticationPrincipal로 주입되지 않았거나 empId가 null인 경우 (예: 필터 순서 등)
        //    SecurityContextHolder에서 직접 Authentication 객체를 가져와 처리 시도
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsDto) {
            UserDetailsDto principal = (UserDetailsDto) authentication.getPrincipal();
            // 여기서도 getUsername() 대신 getEmpId()를 사용해야 합니다.
            if (principal.getEmpId() != null) { // empId가 null이 아닌지 확인
                authService.logout(principal.getEmpId());
                return ResponseEntity.ok().build();
            }
        }

        // 어떤 방법으로도 유효한 사용자 정보를 얻지 못한 경우
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 또는 다른 적절한 상태
    }

    // Refresh Token을 Request Body로 받는 예시
    // 프론트엔드와 협의하여 헤더(X-Refresh-Token) 또는 HttpOnly 쿠키로 받을 수도 있습니다.
    @PostMapping("/reissue")
    public ResponseEntity<TokenDto> reissue(@RequestBody TokenDto requestTokenDto) { // 간단히 TokenDto 재활용 또는 ReissueRequestDto 생성
        TokenDto tokenDto = authService.reissueToken(requestTokenDto.getRefreshToken());
        return ResponseEntity.ok(tokenDto);
    }
}