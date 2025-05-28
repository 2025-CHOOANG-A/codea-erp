package kr.co.codea.productplan;

import java.util.List;


public interface ProductPlanService {
	
	List<ProductPlanDTO> ProductPlanList(ProductPlanDTO dto);
	int insertProductPlan(ProductPlanDTO dto);
	List<ProductPlanDTO> searchItem(String query); //item 검색 api
	ProductPlanDTO productPlanDetail(String planId);
	int productPlanUpdate(ProductPlanDTO dto);
	


}
