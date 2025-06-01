package kr.co.codea.shipment;

import java.io.PrintWriter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/shipment")
public class ShipmentController {

	@Autowired
	ShipmentDAO dao;
	
	PrintWriter pw = null;
	
	@GetMapping("/list")
	public String listPage(@RequestParam(name="sourceDocType", required=false) Integer sourceDocType,
			@RequestParam(name="field", required=false) String field,
			@RequestParam(name="keyword", required=false) String keyword,
			Model m) {	// 출고 목록 페이지
		List<ShipmentDTO> list = this.dao.ship_list(sourceDocType, field, keyword);
		
		m.addAttribute("list", list);
		m.addAttribute("sourceDocType", sourceDocType);
		m.addAttribute("field", field);
		m.addAttribute("keyword", keyword);
		
		return "shipment/shipment_list"; // templates/shipment/shipment_list.html
	}
	
	@GetMapping("/detail")
	public String detailPage(@RequestParam(name="inoutId") int inoutId,
			@RequestParam(name="sourceDocType") int sourceDocType, Model m) {	// 출고 상세 페이지
		ShipmentDTO detail = this.dao.ship_detail(inoutId, sourceDocType);
		m.addAttribute("detail", detail);
		
		return "shipment/shipment_detail"; // templates/shipment/shipment_detail.html
	}
	
	@GetMapping("/write")
	public String writePage() {
		
		return "shipment/shipment_write"; // templates/shipment/shipment_write.html
	}
	
	@GetMapping("/searchItem")
	@ResponseBody
	public List<ShipmentDTO> sea_item(@RequestParam(name="sourceDocType") int sourceDocType, 
			@RequestParam(name="docDate") String docDate){	// 등록 페이지 제품 검색
		List<ShipmentDTO> sea_item = this.dao.ship_sea_item(sourceDocType, docDate);
		
		return sea_item;
	}
	
	@GetMapping("/searchWh")
	@ResponseBody
	public List<ShipmentDTO> sea_wh(@RequestParam(name="whName") String whName){	// 등록 페이지 창고 검색
		List<ShipmentDTO> sea_wh = this.dao.ship_sea_wh(whName);
		
		return sea_wh;
	}
	
	@GetMapping("/searchEmp")
	@ResponseBody
	public List<ShipmentDTO> sea_emp(@RequestParam(name="empName") String empName){	// 등록 페이지 담당자 검색
		List<ShipmentDTO> sea_emp = this.dao.ship_sea_emp(empName);
		
		return sea_emp;
	}
	
	@GetMapping("/writeCk")
	@ResponseBody
	public String ship_check(@RequestParam("sourceDocType") int sourceDocType, 
			@RequestParam("sourceDocHeaderId") int sourceDocHeaderId, 
			@RequestParam("itemId") int itemId) {	// 출고 중복 체크
		String msg = this.dao.check(sourceDocType, sourceDocHeaderId, itemId);
		
		return msg;
	}
	
	@PostMapping("/writeInsert")
	public String write_insert(@ModelAttribute ShipmentDTO dto, HttpServletResponse res) throws Exception {
		res.setContentType("text/html; charset=utf-8");
		this.pw = res.getWriter();
		
		int result = this.dao.ship_insert(dto);
		
		if(result > 0) {
			this.pw.print("<script>"
					+ "alert('출고 등록이 완료되었습니다.');"
					+ "location.href='/shipment/list';"
					+ "</script>");
		}
		else {
			this.pw.print("<script>"
					+ "alert('서비스 오류로 인하여 출고 등록이 완료되지 않았습니다.');"
					+ "history.go(-1);"
					+ "</script>");
		}
		
		this.pw.close();
		
		return null;
	}
}
