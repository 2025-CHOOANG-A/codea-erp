package kr.co.codea.productplan;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProductPlanMapper {
    
	int materialout(); //작업시작에 따른 자재출고 mrp가없음
	ProductPlanDTO findByPlanIdAndStatus(@Param("planId") String planId, @Param("status") String status);
    int updateStatus(@Param("planId") String planId, @Param("targetStatus") String targetStatus);
	List<ProductPlanDTO> ProductPlanList(ProductPlanDTO dto);
	int insertProductPlan(ProductPlanDTO dto); //insert
	List<ProductPlanDTO> searchItem(String query); //item 검색 api
	ProductPlanDTO productPlanDetail(String planId); //생산계획 상세
	int productPlanUpdate(ProductPlanDTO dto); //생산계획 수정
	
	// 자재 소요량 관련 메소드들
    List<MaterialRequirementDTO> getMaterialRequirements(@Param("planId") String planId);
    
    // 재고 관련 메소드들
    int getAvailableInventory(@Param("itemId") int itemId);
    int getAvailableInventoryByWarehouse(@Param("itemId") int itemId, @Param("whId") int whId);
    
    // 가출고 처리 메소드들
    int insertOutbound(@Param("material") MaterialRequirementDTO material, @Param("planId") String planId);
    int updateInventoryAllocated(@Param("itemId") int itemId, @Param("whId") int whId, @Param("quantity") int quantity);
    
    // 작업지시 취소 관련 메소드들
    int deleteOutboundsByPlanId(@Param("planId") String planId);
    int restoreInventoryAllocated(@Param("planId") String planId);
    List<Map<String, Object>> getOutboundsByPlanId(@Param("planId") String planId);
    
    // 자재 현황 조회 메소드들
    List<Map<String, Object>> getMaterialInventoryStatus(@Param("planId") String planId);
    Map<String, Object> getMaterialSummary(@Param("planId") String planId);



}
