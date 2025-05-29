package kr.co.codea.receiving;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/receiving")
public class ReceivingController {
	
	@Autowired
	ReceivingDAO dao;
	
	@GetMapping("/list")
	public String listPage(Model m) {	// 입고 목록 페이지
		List<ReceivingDTO> list = this.dao.rec_list();
		m.addAttribute("list", list);
		
		return "receiving/receiving_list"; // templates/receiving/receiving_list.html
	}

	@GetMapping("/detail")
	public String detailPage(@RequestParam("inoutId") int inoutId, Model m) {	// 입고 상세 페이지
		ReceivingDTO detail = this.dao.rec_detail(inoutId);
		m.addAttribute("detail", detail);
		
    	return "receiving/receiving_detail"; // templates/receiving/receiving_detail.html
	}
    	
	@GetMapping("/write")
	public String writePage() {	// 입고 등록 페이지
    	return "receiving/receiving_write"; // templates/receiving/receiving_write.html
	}
}