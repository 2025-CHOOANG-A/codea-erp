package kr.co.codea.inventory;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/inventory")
public class InventoryController {

	@Autowired
	InventoryDAO dao;
	
	@GetMapping("/list")
	public String listPage(Model m) {	// 재고 목록 페이지

    	return "inventory/inventory_list"; // templates/receiving/receiving_list.html
	}
	
	@GetMapping("/detail")
	public String detailPage() {	// 재고 상세 페이지
    	return "inventory/inventory_detail"; // templates/receiving/receiving_detail.html
	}
	
	@GetMapping("/write")
	public String writePage() {	// 재고 등록 페이지
 
    	return "inventory/inventory_write"; // templates/receiving/receiving_write.html
	}
	
	@GetMapping("/modify")
	public String modifyPage() {	// 재고 수정 페이지
    	return "inventory/inventory_modify"; // templates/receiving/receiving_detail.html
	}
}