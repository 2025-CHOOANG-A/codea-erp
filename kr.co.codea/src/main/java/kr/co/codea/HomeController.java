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
            // 로그인 사용자 정보를 모델에 저장하여 화면에 전달
            model.addAttribute("userLoginId", userDetails.getUsername()); // 로그인 ID (EMP_USER_ID)
            model.addAttribute("userRealName", userDetails.getEmpName()); // 사용자 이름 (EMP_NAME)
            model.addAttribute("empId", userDetails.getEmpId());          // 직원 고유 ID (EMP_ID)

            // [디버그 로그] 로그인한 사용자 정보 출력
//            System.out.println("인증된 사용자: loginId=" + userDetails.getUsername() +  // 로그인 ID
//                               ", realName=" + userDetails.getEmpName() +              // 사용자 이름
//                               ", EMP_ID=" + userDetails.getEmpId());                  // 직원 고유 ID
        } else {
            // [디버그 로그] 비로그인 상태에서 /index 요청 시 메시지 출력
//            System.out.println("/index 요청: 인증된 사용자가 없습니다.");
        }

        return "index"; // templates/index.html 반환
    }
}
