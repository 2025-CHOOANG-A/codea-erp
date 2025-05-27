package kr.co.codea;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    // 로그인 페이지 진입
    @GetMapping("/login")
    public String loginPage() {
        return "auth/login"; // templates/auth/login.html
    }

    // 루트 경로 접근 시 로그인 페이지 반환
    @GetMapping("/")
    public String homePage() {
        return "auth/login"; // 필요 시 redirect:/login 으로 변경 가능
    }
}
