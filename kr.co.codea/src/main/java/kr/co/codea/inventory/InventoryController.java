package kr.co.codea.inventory;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/inventory")
public class InventoryController {

	@Autowired
	InventoryDAO dao;
	
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
	
	
	@GetMapping("/write")
	public String writePage(@RequestParam int itemId, @RequestParam int whId, Model m) {	// 재고 등록 페이지
		InventoryDTO dto = this.dao.inv_dto(itemId, whId);
		m.addAttribute("dto", dto);
		
		return "inventory/inventory_write"; // templates/receiving/receiving_write.html
	}
	
	@GetMapping("/modify")
	public String modifyPage() {	// 재고 수정 페이지
    	return "inventory/inventory_modify"; // templates/receiving/receiving_detail.html
	}
}