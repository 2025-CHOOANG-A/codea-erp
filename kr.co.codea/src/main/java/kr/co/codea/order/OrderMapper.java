package kr.co.codea.order;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderMapper {

    /**
     * 주문 목록 조회 (페이징)
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
     */
    int orderCount(
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );

    /**
     * 가출고 정보를 INOUT 테이블에 삽입
     */
    int insertProvisionalShipmentToInOut(OrderDto.ProvisionalShipmentRequest request);

    /**
     * 특정 주문상세(ORD_DETAIL)의 현재 상태 조회
     */
    String selectOrderDetailStatus(@Param("ordDetailId") Long ordDetailId);

    /**
     * ORD_DETAIL 상태를 '완료'로 변경
     */
    int updateOrderDetailStatusToCompleted(@Param("ordDetailId") Long ordDetailId);

    /**
     * 같은 ORD_ID의 남아있는 (완료되지 않은) 주문상세 개수 조회
     */
    int countRemainingOrderDetail(@Param("ordId") Long ordId);

    /**
     * ORD_HEADER 상태를 '완료'로 변경
     */
    int updateOrderHeaderStatusToCompleted(@Param("ordId") Long ordId);

    /**
     * ORD_HEADER 상태를 '진행중'으로 변경
     */
    int updateOrderHeaderStatusToInProgress(@Param("ordId") Long ordId);

    /**
     * ORD_HEADER의 현재 상태 조회
     */
    String selectOrderHeaderStatus(@Param("ordId") Long ordId);
}
