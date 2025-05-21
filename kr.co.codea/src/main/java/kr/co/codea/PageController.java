package kr.co.codea;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
	 // 사용자가 "/login" 경로로 GET 요청을 보내면 login.html을 보여줌
    @GetMapping("/login")
    public String loginPage() {
        return "auth/login"; // src/main/resources/templates/login.html 파일을 의미
    }

    // 만약 루트 경로("/")로 접속했을 때 로그인 페이지를 보여주고 싶다면:
    @GetMapping("/")
    public String homePage() {
        // 이미 인증된 사용자는 다른 페이지로 리다이렉션하고,
        // 그렇지 않으면 로그인 페이지로 보낼 수도 있습니다.
        // 여기서는 간단히 로그인 페이지로 가정합니다.
        return "auth/login"; // 또는 "redirect:/login"
    }
}
