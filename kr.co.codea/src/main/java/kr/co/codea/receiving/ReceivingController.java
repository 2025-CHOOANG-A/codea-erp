package kr.co.codea.receiving;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/receiving")
public class ReceivingController {

	@GetMapping("/detail")
	public String detailPage() {	// 입고 상세 페이지
    	return "receiving/receiving_detail"; // templates/receiving/receiving_detail.html
	}
    
	@GetMapping("/list")
	public String listPage() {	// 입고 목록 페이지
    	return "receiving/receiving_list"; // templates/receiving/receiving_list.html
	}
	
	@GetMapping("/write")
	public String writePage() {	// 입고 등록 페이지
    	return "receiving/receiving_write"; // templates/receiving/receiving_write.html
	}
}