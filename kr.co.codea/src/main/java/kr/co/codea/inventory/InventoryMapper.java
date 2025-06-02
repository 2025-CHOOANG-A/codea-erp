package kr.co.codea.inventory;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface InventoryMapper {
	public List<InventoryDTO> inv_list(@Param("itemType") String itemType, @Param("field") String field, @Param("keyword") String keyword);	// 목록 페이지
	
	public InventoryDTO inv_detail(@Param("inventoryId") int inventoryId);	// 상세 페이지
	
	public List<InventoryDTO> inout(@Param("itemId") int itemId, @Param("whId") int whId);	// 입출고 내역
	
	public List<InventoryDTO> inv_sea_item(@Param("itemName") String itemName);	// 등록 및 수정 페이지 제품 검색
	
	public List<InventoryDTO> inv_sea_wh(@Param("whName") String whName);	// 등록 및 수정 페이지 창고 검색
	
	public List<InventoryDTO> inv_sea_emp(@Param("empName") String empName);	// 등록 및 수정 페이지 담당자 검색
	
	public Integer in_qty_pur(@Param("itemId") int itemId, @Param("whId") int whId);	// 원자재 입고 수량
	
	public Integer out_qty_pur(@Param("itemId") int itemId, @Param("whId") int whId);	// 원자재 출고 수량
	
	public Integer in_qty_pro(@Param("itemId") int itemId, @Param("whId") int whId);	// 완제품 입고 수량
	
	public Integer out_qty_pro(@Param("itemId") int itemId, @Param("whId") int whId);	// 완제품 출고 수량
	
	public Integer inv_in_qty(@Param("itemId") int itemId, @Param("whId") int whId);	// 입고 예정 수량
	
	public Integer inv_out_qty(@Param("itemId") int itemId, @Param("whId") int whId);	// 출고 예정 수량
	
	public InventoryDTO in_data(@Param("itemId") int itemId, @Param("whId") int whId, @Param("itemType") String itemType);	// 입고 수량 및 단가
	
	public InventoryDTO inv_avg_cost(@Param("itemId") int itemId, @Param("whId") int whId);	// 기존 평균 단가
	
	public Integer inv_check(@Param("itemId") int itemId, @Param("whId") int whId);	// 재고 중복 체크
	
	public Integer inout_check(@Param("itemId") int itemId, @Param("whId") int whId);	// 입출고 중복 체크
	
	public int inv_insert(InventoryDTO dto);	// 재고 등록
	
	public Integer inv_id(@Param("itemId") int itemId, @Param("whId") int whId);	// 재고 번호 조회
	
	public int inv_log_insert(InventoryDTO dto);	// 재고 로그 등록
	
	public InventoryDTO inv_mod(@Param("inventoryId") int inventoryId);	// 수정 페이지
	
	public int inv_update(InventoryDTO dto);	// 재고 수정
	
	public InventoryDTO inv_before(@Param("itemId") int itemId, @Param("whId") int whId);	// 기존 재고 및 평균 단가 조회
	
	public int inv_log_update(InventoryDTO dto);	// 재고 로그 수정
}