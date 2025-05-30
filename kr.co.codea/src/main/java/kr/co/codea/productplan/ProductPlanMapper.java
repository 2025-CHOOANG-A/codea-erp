package kr.co.codea.productplan;

import java.util.List;

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



}
