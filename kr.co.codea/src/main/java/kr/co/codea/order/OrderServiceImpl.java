package kr.co.codea.order;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final int DEFAULT_PAGE_SIZE = 10;

    @Autowired
    public OrderServiceImpl(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }

    @Override
    public OrderDto.PagingResult<OrderDto.OrderDetailView> getOrderList(
            String keyword, String status,
            String startDate, String endDate,
            int page, int size) {

        // 0) 페이징 인덱스 계산
        int currentPage = page < 0 ? 0 : page;
        int pageSize = size <= 0 ? DEFAULT_PAGE_SIZE : size;
        int offset = currentPage * pageSize;

        // 1) 총 개수 조회
        int totalCount = orderMapper.orderCount(keyword, status, startDate, endDate);

        // 2) 페이징된 목록 조회
        List<OrderDto.OrderDetailView> list =
                orderMapper.selectOrderList(keyword, status, startDate, endDate, offset, pageSize);

        // 3) 전체 페이지 수 계산
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);

        // 4) PagingResult 반환
        return new OrderDto.PagingResult<>(list, currentPage, totalPages, totalCount);
    }

    @Override
    public OrderDto.OrderHeaderDetail getOrderHeaderDetail(Long ordId) {
        return orderMapper.selectOrderHeaderDetailById(ordId);
    }

    @Override
    public List<OrderDto.OrderItemDetail> getOrderItems(Long ordId) {
        return orderMapper.selectOrderItemsByOrderId(ordId);
    }

    @Override
    public boolean processShipment(OrderDto.ProvisionalShipmentRequest request) {
        // 1) 해당 ORD_DETAIL의 상태를 먼저 확인
        String detailStatus = orderMapper.selectOrderDetailStatus(request.getOrderDetailId());
        if (!"진행중".equals(detailStatus) && !"접수".equals(detailStatus)) {
            // 이미 완료이거나 취소된 항목이라면 처리 불가
            return false;
        }

        // 2) INOUT 테이블에 가출고 정보 삽입
        orderMapper.insertProvisionalShipmentToInOut(request);

        // 3) 해당 ORD_DETAIL 상태를 '완료'로 변경
        orderMapper.updateOrderDetailStatusToCompleted(request.getOrderDetailId());

        // 4) 같은 ORD_ID에서 남은 완료 전(STATUS != '완료') 상세 개수 확인
        int remaining = orderMapper.countRemainingOrderDetail(request.getOrdId());
        if (remaining == 0) {
            // 5) 남은 상세가 없으면 ORD_HEADER 상태를 '완료'로 변경
            orderMapper.updateOrderHeaderStatusToCompleted(request.getOrdId());
        } else {
            // 6) 남은 상세가 있으면 ORD_HEADER 상태를 '진행중'으로 변경
            orderMapper.updateOrderHeaderStatusToInProgress(request.getOrdId());
        }

        return true;
    }
}
