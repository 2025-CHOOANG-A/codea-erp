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
            // Model에 추가하는 어트리뷰트 이름을 명확히 구분
            // userLoginId: 로그인 시 사용한 ID (EMP_USER_ID)
            // userRealName: 실제 사용자 이름 (EMP_NAME)
            // empId: 데이터베이스의 직원 고유 ID (EMP_ID)
            model.addAttribute("userLoginId", userDetails.getUsername()); // UserDetails.getUsername()은 empUserId를 반환
            model.addAttribute("userRealName", userDetails.getEmpName());   // ✨ UserDetailsDto에 추가된 getEmpName() 호출 ✨
            model.addAttribute("empId", userDetails.getEmpId());

            System.out.println("인증된 사용자: loginId=" + userDetails.getUsername() +
                               ", realName=" + userDetails.getEmpName() + // ✨ 로그에도 실제 이름 추가 ✨
                               ", EMP_ID=" + userDetails.getEmpId());
        } else {
            System.out.println("/index 요청: 인증된 사용자가 없습니다.");
        }
        return "index"; // src/main/resources/templates/index.html을 찾습니다.
    }
}