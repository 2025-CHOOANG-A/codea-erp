package kr.co.codea.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/members")
    public String listMembers(Model model) {
        model.addAttribute("members", memberService.getAllMembers());
        return "member/list"; // templates/member/list.html
    }
}