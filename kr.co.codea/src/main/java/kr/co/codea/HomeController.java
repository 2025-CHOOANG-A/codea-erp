package kr.co.codea;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import kr.co.codea.auth.dto.UserDetailsDto;

@Controller
public class HomeController {

    @GetMapping("/index")
    public String indexPage(@AuthenticationPrincipal UserDetailsDto userDetails, Model model) {
        if (userDetails != null) {
            // 로그인된 사용자 정보 모델에 전달
            model.addAttribute("userLoginId", userDetails.getUsername());  // 사용자 ID (EMP_USER_ID)
            model.addAttribute("userRealName", userDetails.getEmpName());  // 사용자 이름 (EMP_NAME)
            model.addAttribute("empId", userDetails.getEmpId());           // 사원 고유 ID (EMP_ID)
        }

        return "index"; // templates/index.html 렌더링
    }
}
