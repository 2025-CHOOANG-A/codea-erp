package kr.co.codea.productplan;

import java.util.List;

import com.github.pagehelper.PageInfo;


public interface ProductPlanService {
	
	List<ProductPlanDTO> ProductPlanList(ProductPlanDTO dto);
	int insertProductPlan(ProductPlanDTO dto);
	List<ProductPlanDTO> searchItem(String query); //item 검색 api
	ProductPlanDTO productPlanDetail(String planId);
	int productPlanUpdate(ProductPlanDTO dto);
	int changePlansStatus(List<String> PlanIds, String mrpok, String targetStatus); //작업지시
	PageInfo<ProductPlanDTO> getpages(ProductPlanDTO dto,  int page, int size);
	


}
