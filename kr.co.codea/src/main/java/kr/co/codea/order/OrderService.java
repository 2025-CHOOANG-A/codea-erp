package kr.co.codea.order;

import java.util.List;

public interface OrderService {

    /**
     * 페이징된 주문 목록 조회
     */
    OrderDto.PagingResult<OrderDto.OrderDetailView> getOrderList(
            String keyword,
            String status,
            String startDate,
            String endDate,
            int page,
            int size
    );

    /**
     * 단일 주문 헤더 + 거래처명·사원명 조회
     */
    OrderDto.OrderHeaderDetail getOrderHeaderDetail(Long ordId);

    /**
     * 단일 주문의 상세(아이템) 목록 조회
     */
    List<OrderDto.OrderItemDetail> getOrderItems(Long ordId);

    /**
     * 가출고 처리 로직 (Controller에서 호출)
     */
    boolean processShipment(OrderDto.ProvisionalShipmentRequest request);
}
