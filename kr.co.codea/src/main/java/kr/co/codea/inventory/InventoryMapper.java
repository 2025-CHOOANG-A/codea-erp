package kr.co.codea.inventory;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface InventoryMapper {
	public List<InventoryDTO> inv_list();	// 목록 페이지
	
	public InventoryDTO inv_detail(@Param("inventoryId") int inventoryId);	// 상세 페이지
	
	public List<InventoryDTO> inv_sea_item(@Param("itemName") String itemName);	// 등록 및 수정 페이지 제품 검색
	
	public List<InventoryDTO> inv_sea_wh(@Param("whName") String whName);	// 등록 및 수정 페이지 창고 검색
	
	public List<InventoryDTO> inv_sea_emp(@Param("empName") String empName);	// 등록 및 수정 페이지 담당자 검색
	
	public int inv_in_qty(@Param("itemId") int itemId, @Param("whId") int whId);	// 입고 예정 수량
	
	public int inv_out_qty(@Param("itemId") int itemId, @Param("whId") int whId);	// 출고 예정 수량
}