package kr.co.codea;

import org.springframework.http.HttpStatus; // HttpStatus를 직접 임포트
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
// import org.springframework.stereotype.Controller; // @RestController 사용 시 불필요
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.co.codea.auth.dto.UserDetailsDto;

//임의의 컨트롤러 (예: HomeController.java)
@RestController
public class HomeController {
    @GetMapping("/index")
    public ResponseEntity<String> indexPage(@AuthenticationPrincipal UserDetailsDto userDetails) {
        if (userDetails != null) {
            return ResponseEntity.ok("Welcome to index, " + userDetails.getUsername() + " (EMP_ID: " + userDetails.getEmpId() + ") with roles: " + userDetails.getAuthorities());
        } else {
            // HttpStatusCode 대신 HttpStatus.UNAUTHORIZED 사용
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }
    }
}