package kr.co.codea.productactual;

import java.util.List;

import com.github.pagehelper.PageInfo;

public interface ProductActualService {
	 // 생산실적 목록 조회
    List<ProductActualDTO> productActualList(ProductActualDTO dto);
    
    // 일일생산수량 등록
    void registerProductActual(ProductActualDTO dto);
    
    // 일일생산수량 조회
    List<ProductActualDTO> getDailyActualsByPlanId(String planId);
    
    // 페이징 처리
    PageInfo<ProductActualDTO> getpages(ProductActualDTO dto, int page, int size);
    
    // 작업 시작
    void startWork(String planId);
    
    // 작업 종료 (완제품 입고 포함)
    void endWork(String planId);

}
