package kr.co.codea.inventory;

import java.util.List;

public interface InventoryService {
	public List<InventoryDTO> inv_list();	// 목록 페이지
	
	public InventoryDTO inv_detail(int inventoryId);	// 상세 페이지
	
	public List<InventoryDTO> inv_sea_item(String itemName);	// 등록 및 수정 페이지 제품 검색
	
	public List<InventoryDTO> inv_sea_wh(String whName);	// 등록 및 수정 페이지 창고 검색
	
	public List<InventoryDTO> inv_sea_emp(String empName);	// 등록 및 수정 페이지 담당자 검색
	
	public InventoryDTO inv_dto(int itemId, int whId);	// dto 관련 메소드 (입고 예정 수량, 출고 예정 수량)
}