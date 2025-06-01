package kr.co.codea.productactual;

import java.util.List;

import com.github.pagehelper.PageInfo;

public interface ProductActualService {
	List<ProductActualDTO> productActualList (ProductActualDTO dto); //작업지시인 리스트 가져오기
	void registerProductActual(ProductActualDTO dto); //일일생산수량 등록
	List<ProductActualDTO> getDailyActualsByPlanId(String planId); 
	PageInfo<ProductActualDTO> getpages(ProductActualDTO dto, int page, int size);

}
