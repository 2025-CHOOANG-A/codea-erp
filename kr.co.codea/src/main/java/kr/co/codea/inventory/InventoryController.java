package kr.co.codea.inventory;

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
@RequestMapping("/inventory")
public class InventoryController {

	@Autowired
	InventoryDAO dao;
	
	PrintWriter pw = null;
	
	@GetMapping("/list")
	public String listPage(@RequestParam(name="itemType", required=false) String itemType,
			@RequestParam(name="field", required=false) String field,
			@RequestParam(name="keyword", required=false) String keyword,
			Model m) {	// 재고 목록 페이지
		List<InventoryDTO> list = this.dao.inv_list(itemType, field, keyword);
		
		m.addAttribute("list", list);
		m.addAttribute("itemType", itemType);
		m.addAttribute("field", field);
		m.addAttribute("keyword", keyword);
		
    	return "inventory/inventory_list"; // templates/receiving/receiving_list.html
	}
	
	@GetMapping("/detail")
	public String detailPage(@RequestParam(name="inventoryId") int inventoryId, Model m) {	// 재고 상세 페이지
		InventoryDTO detail = this.dao.inv_detail(inventoryId);
		m.addAttribute("detail", detail);
		
    	return "inventory/inventory_detail"; // templates/receiving/receiving_detail.html
	}
	
	@GetMapping("/searchItem")
	@ResponseBody
	public List<InventoryDTO> sea_item(@RequestParam(name="itemName") String itemName){	// 등록 및 수정 페이지 제품 검색
		List<InventoryDTO> sea_item =  this.dao.inv_sea_item(itemName);
		
		return sea_item;
	}
	
	@GetMapping("/searchWh")
	@ResponseBody
	public List<InventoryDTO> sea_wh(@RequestParam(name="whName") String whName){	// 등록 및 수정 페이지 창고 검색
		List<InventoryDTO> sea_wh =  this.dao.inv_sea_wh(whName);
		
		return sea_wh;
	}
	
	@GetMapping("/searchEmp")
	@ResponseBody
	public List<InventoryDTO> sea_emp(@RequestParam(name="empName") String empName){	// 등록 및 수정 페이지 담당자 검색
		List<InventoryDTO> sea_emp =  this.dao.inv_sea_emp(empName);
		
		return sea_emp;
	}
	
	@GetMapping("/curQty")
	@ResponseBody
	public int cur_qty(@RequestParam(name="itemId") int itemId,
			@RequestParam(name="whId") int whId,
			@RequestParam(name="itemType") String itemType) {	// 재고 보유 수량
		int currentQty = this.dao.inv_qty(itemId, whId, itemType);
		
		return currentQty;
	}
	
	@GetMapping("/writeCk")
	@ResponseBody
	public String inv_check(@RequestParam(name="itemId") int itemId,
			@RequestParam(name="whId") int whId) {	// 중복 체크
		String msg = this.dao.check(itemId, whId);
		
		return msg;
	}
	
	@GetMapping("/write")
	public String writePage() {	// 재고 등록 페이지
		
		return "inventory/inventory_write"; // templates/receiving/receiving_write.html
	}
	
	@PostMapping("/writeInsert")
	public String write_insert(@ModelAttribute InventoryDTO dto, HttpServletResponse res) throws Exception {	// 재고 등록
		res.setContentType("text/html; charset=utf-8");
		this.pw = res.getWriter();
		
		int result = this.dao.inv_insert(dto);
		
		if(result > 0) {
			this.pw.print("<script>"
					+ "alert('재고 등록이 완료되었습니다.');"
					+ "location.href='/inventory/list';"
					+ "</script>");
		}
		else {
			this.pw.print("<script>"
					+ "alert('서비스 오류로 인하여 재고 등록이 완료되지 않았습니다.');"
					+ "history.go(-1);"
					+ "</script>");
		}
		
		this.pw.close();
		
		return null;
	}
	
	@GetMapping("/modify")
	public String modifyPage(@RequestParam(name="inventoryId") int inventoryId, Model m) {	// 재고 수정 페이지
		InventoryDTO mod = this.dao.inv_mod(inventoryId);
		m.addAttribute("mod", mod);
		
    	return "inventory/inventory_modify"; // templates/receiving/receiving_detail.html
	}
	
	@PostMapping("/modUpdate")
	public String modify_update(@ModelAttribute InventoryDTO dto, HttpServletResponse res) throws Exception {	// 재고 수정
		res.setContentType("text/html; charset=utf-8");
		this.pw = res.getWriter();
		
		int result = this.dao.inv_update(dto);
		
		if(result > 0) {
			this.pw.print("<script>"
					+ "alert('재고 수정이 완료되었습니다.');"
					+ "location.href='/inventory/detail?inventoryId=" + dto.getInventoryId() + "';"
					+ "</script>");
		}
		else {
			this.pw.print("<script>"
					+ "alert('서비스 오류로 인하여 재고 수정이 완료되지 않았습니다.');"
					+ "history.go(-1);"
					+ "</script>");
		}
		
		this.pw.close();
		
		return null;
	}
}