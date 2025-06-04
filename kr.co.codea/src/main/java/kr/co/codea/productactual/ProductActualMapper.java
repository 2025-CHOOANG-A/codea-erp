package kr.co.codea.productactual;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductActualMapper {
    // 생산실적 목록 조회
    List<ProductActualDTO> productActualList(ProductActualDTO dto);
    
    // 일일생산수량 등록
    void registerProductActual(ProductActualDTO dto);
    
    // 일일생산수량 조회
    List<ProductActualDTO> getDailyActualsByPlanId(String planId);
    
    // 생산계획 실제수량 업데이트
    void updateProductionPlanActualQty(String planId);
    
    // 작업 시작 (상태 변경: 작업지시 → 진행중)
    int startWork(String planId);
    
    // 작업 종료 (상태 변경: 진행중 → 완료)
    int endWork(String planId);
    
    // 완제품 입고용 생산계획 정보 조회
    ProductActualDTO getProductionPlanForReceiving(String planId);

}
