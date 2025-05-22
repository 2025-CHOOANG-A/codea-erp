package kr.co.codea.inventory;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.ui.Model;

@Component
public class m_inventory_search {
	
	@Autowired
	InventoryDAO dao;
	
	public void m_inventory_search(Model m) {
		List<InventoryDTO> sea_item = this.dao.inv_sea_item();	// 제품 검색
		m.addAttribute("sea_item", sea_item);
		
		List<InventoryDTO> sea_wh = this.dao.inv_sea_wh();	// 창고 검색
		m.addAttribute("sea_wh", sea_wh);
		
		List<InventoryDTO> sea_emp = this.dao.inv_sea_emp();	// 담당자 검색
		m.addAttribute("sea_emp", sea_emp);
	}
}