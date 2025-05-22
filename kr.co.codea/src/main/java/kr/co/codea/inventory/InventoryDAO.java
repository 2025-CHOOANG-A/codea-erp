package kr.co.codea.inventory;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InventoryDAO implements InventoryService {
	
	@Autowired
	private InventoryMapper mp;
	
	@Override
	public List<InventoryDTO> inv_list() {	// 목록 페이지
		List<InventoryDTO> list = this.mp.inv_list();
		
		return list;
	}
	
	@Override
	public InventoryDTO inv_detail(int inventoryId) {	// 상세 페이지
		InventoryDTO detail = this.mp.inv_detail(inventoryId);
		
		return detail;
	}

	@Override
	public List<InventoryDTO> inv_sea_item() {	// 등록 및 수정 페이지 제품 검색
		List<InventoryDTO> sea_item = this.mp.inv_list();
		
		return sea_item;
	}
	
	@Override
	public List<InventoryDTO> inv_sea_wh() {	// 등록 및 수정 페이지 창고 검색
		List<InventoryDTO> sea_wh = this.mp.inv_sea_wh();
		
		return sea_wh;
	}
	
	@Override
	public List<InventoryDTO> inv_sea_emp() {	// 등록 및 수정 페이지 담당자 검색
		List<InventoryDTO> sea_emp = this.mp.inv_sea_emp();
		
		return sea_emp;
	}
}