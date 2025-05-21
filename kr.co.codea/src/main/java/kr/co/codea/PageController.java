package kr.co.codea;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/login")
    public String loginPage() {
        // 로그인 페이지 요청 시: templates/auth/login.html 반환
        return "auth/login";
    }

    @GetMapping("/")
    public String homePage() {
        // 루트 경로 접근 시: 로그인 페이지로 이동
        // (추후 인증 여부에 따라 리다이렉션 처리 가능)
        return "auth/login"; // 또는 "redirect:/login"
    }
}
