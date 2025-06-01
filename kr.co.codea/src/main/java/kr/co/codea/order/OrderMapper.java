package kr.co.codea.order;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderMapper {
    
    /**
     * 주문 목록 조회 (페이징)
     * @param keyword 검색 키워드 (거래처명 또는 제품명)
     * @param status 주문 상태 (진행중, 완료 등)
     * @param startDate 시작일 (yyyy-MM-dd 형식)
     * @param endDate 종료일 (yyyy-MM-dd 형식)
     * @param offset 페이징 오프셋
     * @param limit 페이징 리미트
     * @return 주문 상세 정보 리스트
     */
    List<OrderDto.OrderDetailView> selectOrderList(
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("offset") int offset,
            @Param("limit") int limit
    );
    
    /**
     * 주문 총 개수 조회
     * @param keyword 검색 키워드 (거래처명 또는 제품명)
     * @param status 주문 상태 (진행중, 완료 등)
     * @param startDate 시작일 (yyyy-MM-dd 형식)
     * @param endDate 종료일 (yyyy-MM-dd 형식)
     * @return 조건에 맞는 주문 총 개수
     */
    int orderCount(
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );
}