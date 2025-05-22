package kr.co.codea.inventory;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface InventoryMapper {
	public List<InventoryDTO> inv_list();	// 목록 페이지
	
	public InventoryDTO inv_detail(@Param("inventoryId") int inventoryId);	// 상세 페이지
	
	public List<InventoryDTO> inv_sea_item();	// 등록 및 수정 페이지 제품 검색
	
	public List<InventoryDTO> inv_sea_wh();	// 등록 및 수정 페이지 창고 검색
	
	public List<InventoryDTO> inv_sea_emp();	// 등록 및 수정 페이지 담당자 검색
}