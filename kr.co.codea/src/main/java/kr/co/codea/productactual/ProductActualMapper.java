package kr.co.codea.productactual;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductActualMapper {
	List<ProductActualDTO> productActualList (ProductActualDTO dto);
	void registerProductActual(ProductActualDTO dto); //일일생산수량 등록
	List<ProductActualDTO> getDailyActualsByPlanId(String planId); 
	void updateProductionPlanActualQty(String planId); 
}
