package kr.co.codea.inventory;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/inventory")
public class InventoryController {

	@Autowired
	InventoryDAO dao;
	
	@Autowired
	m_inventory_search search;
	
	@GetMapping("/list")
	public String listPage(Model m) {	// 재고 목록 페이지
		List<InventoryDTO> list = this.dao.inv_list();
		m.addAttribute("list", list);
		
    	return "inventory/inventory_list"; // templates/receiving/receiving_list.html
	}
	
	@GetMapping("/detail")
	public String detailPage(@RequestParam("inventoryId") int inventoryId, Model m) {	// 재고 상세 페이지
		InventoryDTO detail = this.dao.inv_detail(inventoryId);
		m.addAttribute("detail", detail);
		
    	return "inventory/inventory_detail"; // templates/receiving/receiving_detail.html
	}
	
	@GetMapping("/write")
	public String writePage(Model m) {	// 재고 등록 페이지
		this.search.m_inventory_search(m);	// 검색
		
    	return "inventory/inventory_write"; // templates/receiving/receiving_write.html
	}
	
	@GetMapping("/modify")
	public String modifyPage() {	// 재고 수정 페이지
    	return "inventory/inventory_modify"; // templates/receiving/receiving_detail.html
	}
}