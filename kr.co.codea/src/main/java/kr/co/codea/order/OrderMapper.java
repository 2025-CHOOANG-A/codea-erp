package kr.co.codea.order;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrderMapper {

    // 1) 주문 목록 조회 (페이징)
    List<OrderDto.OrderDetailView> selectOrderList(
            @Param("keyword") String keyword,
            @Param("status")  String status,
            @Param("startDate") String startDate,
            @Param("endDate")   String endDate,
            @Param("offset")    int offset,
            @Param("limit")     int limit
    );

    // 2) 주문 총 개수 조회 (페이징용)
    int orderCount(
            @Param("keyword") String keyword,
            @Param("status")  String status,
            @Param("startDate") String startDate,
            @Param("endDate")   String endDate
    );

    // 3) 가출고 정보 INOUT 테이블에 삽입
    void insertProvisionalShipmentToInOut(OrderDto.ProvisionalShipmentRequest request);

    // 4) 헤더 상태 조회
    String selectOrderHeaderStatus(Long ordId);

    // 5) ORD_DETAIL 상태 조회
    String selectOrderDetailStatus(Long ordDetailId);

    // 6) ORD_DETAIL 상태를 '완료'로 업데이트
    void updateOrderDetailStatusToCompleted(Long ordDetailId);

    // 7) 같은 ORD_ID에서 아직 완료되지 않은 상세 개수 조회
    int countRemainingOrderDetail(Long ordId);

    // 8) ORD_HEADER 상태를 '완료'로 변경
    void updateOrderHeaderStatusToCompleted(Long ordId);

    // 9) ORD_HEADER 상태를 '진행중'으로 변경
    void updateOrderHeaderStatusToInProgress(Long ordId);

    // =================== 아래부터 “주문 상세” 기능을 위한 메서드 ===================

    /**
     * 10) 단일 주문 헤더 + 거래처명·사원명 포함 조회
     *     resultType = "kr.co.codea.order.OrderDto$OrderHeaderDetail"
     */
    OrderDto.OrderHeaderDetail selectOrderHeaderDetailById(Long ordId);

    /**
     * 11) 해당 ORD_ID에 속한 모든 주문 상세(아이템) 조회
     *     resultType = "kr.co.codea.order.OrderDto$OrderItemDetail"
     */
    List<OrderDto.OrderItemDetail> selectOrderItemsByOrderId(Long ordId);
}
