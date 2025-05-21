package kr.co.codea.notice;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/notice")
@PreAuthorize("hasRole('ROLE_ADMIN')") // ✨ ADMIN 역할만 이 메소드 호출 가능 ✨
@RequiredArgsConstructor
public class NoticeController {
	@GetMapping("/list")
	public String noticeList() {
		return "notice/notice_list";
	}

}
