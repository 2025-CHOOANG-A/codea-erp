package kr.co.codea.inventory;

import java.util.List;

public interface InventoryService {
	public List<InventoryDTO> inv_list(String itemType, String field, String keyword);	// 목록 페이지
	
	public InventoryDTO inv_detail(int inventoryId);	// 상세 페이지
	
	public List<InventoryDTO> inout(int itemId, int whId);	// 입출고 내역
	
	public List<InventoryDTO> inv_sea_item(String itemName);	// 등록 및 수정 페이지 제품 검색
	
	public List<InventoryDTO> inv_sea_wh(String whName);	// 등록 및 수정 페이지 창고 검색
	
	public List<InventoryDTO> inv_sea_emp(String empName);	// 등록 및 수정 페이지 담당자 검색
	
	public Integer inv_qty(int itemId, int whId, String itemType);	// 보유 수량

	public InventoryDTO inv_dto(int itemId, int whId);	// dto 관련 메소드 (입고 예정 수량, 출고 예정 수량)
	
	public InventoryDTO avg_cost(int currentQty, double averageCost, int itemId, int whId, String itemType);	// 평균 단가 (기존 수량, 기존 평균 단가, 입고 수량, 입고 단가)
	
	public String check(int itemId, int whId);	// 중복 체크
	
	public Integer inv_insert(InventoryDTO dto);	// 재고 등록
	
	public InventoryDTO inv_mod(int inventoryId);	// 수정 페이지
	
	public Integer inv_update(InventoryDTO dto);	// 재고 수정
	
	public InventoryDTO inv_before(int itemId, int whId);	// 기존 재고 및 평균 단가 조회
}