package kr.co.codea.productplan;

import java.util.List;
import java.util.Map;

import com.github.pagehelper.PageInfo;


public interface ProductPlanService {
	
	List<ProductPlanDTO> ProductPlanList(ProductPlanDTO dto);
	int insertProductPlan(ProductPlanDTO dto);
	List<ProductPlanDTO> searchItem(String query); //item 검색 api
	ProductPlanDTO productPlanDetail(String planId);
	int productPlanUpdate(ProductPlanDTO dto);
	PageInfo<ProductPlanDTO> getpages(ProductPlanDTO dto,  int page, int size);
	
    // 작업지시 관련 메소드들 (수정됨)
    Map<String, Object> changePlansStatus(List<String> planIds, String targetStatus, String mrpok);
    Map<String, Object> cancelWorkOrders(List<String> planIds);
    
    // 자재 소요량 관련 메소드들 (새로 추가)
    List<MaterialRequirementDTO> getMaterialRequirements(String planId);
    int getAvailableInventory(int itemId);
    Map<String, Object> checkMaterialAvailability(List<String> planIds);
    boolean validateMaterialStock(String planId);
	


}
