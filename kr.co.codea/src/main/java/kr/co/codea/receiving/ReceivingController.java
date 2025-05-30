package kr.co.codea.receiving;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/receiving")
public class ReceivingController {
	
	@Autowired
	ReceivingDAO dao;
	
	@GetMapping("/list")
	public String listPage(@RequestParam(name="itemType", required=false) String itemType,
			@RequestParam(name="field", required=false) String field,
			@RequestParam(name="keyword", required=false) String keyword,
			Model m) {	// 입고 목록 페이지
		List<ReceivingDTO> list = this.dao.rec_list(itemType, field, keyword);
		
		m.addAttribute("list", list);
		m.addAttribute("itemType", itemType);
		m.addAttribute("field", field);
		m.addAttribute("keyword", keyword);
		
		return "receiving/receiving_list"; // templates/receiving/receiving_list.html
	}

	@GetMapping("/detail")
	public String detailPage(@RequestParam(name="inoutId") int inoutId,
			@RequestParam(name="sourceDocType") int sourceDocType, Model m) {	// 입고 상세 페이지
		ReceivingDTO detail = this.dao.rec_detail(inoutId, sourceDocType);
		m.addAttribute("detail", detail);
		
    	return "receiving/receiving_detail"; // templates/receiving/receiving_detail.html
	}
    	
	@GetMapping("/write")
	public String writePage() {	// 입고 등록 페이지
    	return "receiving/receiving_write"; // templates/receiving/receiving_write.html
	}
	
	@GetMapping("/searchItem")
	@ResponseBody
	public List<ReceivingDTO> sea_item(@RequestParam(name="sourceDocType") int sourceDocType, 
			@RequestParam(name="docDate") String docDate){	// 등록 페이지 제품 검색
		List<ReceivingDTO> sea_item = this.dao.rec_sea_item(sourceDocType, docDate);
		
		return sea_item;
	}
	
	@GetMapping("/searchWh")
	@ResponseBody
	public List<ReceivingDTO> sea_wh(@RequestParam(name="whName") String whName){	// 등록 페이지 창고 검색
		List<ReceivingDTO> sea_wh = this.dao.rec_sea_wh(whName);
		
		return sea_wh;
	}
	
	@GetMapping("/searchEmp")
	@ResponseBody
	public List<ReceivingDTO> sea_emp(@RequestParam(name="empName") String empName){	// 등록 페이지 담당자 검색
		List<ReceivingDTO> sea_emp = this.dao.rec_sea_emp(empName);
		
		return sea_emp;
	}
}