package kr.co.codea.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;

    @Override
    public List<OrderDto.OrderDetailView> getPagedOrders(
            String keyword,
            String status,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size
    ) {
        if (page < 0) {
            log.warn("Invalid page number: {}. Setting to 0.", page);
            page = 0;
        }
        if (size <= 0) {
            log.warn("Invalid page size: {}. Setting to 10.", size);
            size = 10;
        }

        String startDateStr = (startDate != null) ? startDate.toString() : null;
        String endDateStr   = (endDate != null)   ? endDate.toString()   : null;
        int offset = page * size;

        log.debug("Searching orders with parameters: keyword={}, status={}, startDate={}, endDate={}, page={}, size={}",
                keyword, status, startDateStr, endDateStr, page, size);

        try {
            List<OrderDto.OrderDetailView> result = orderMapper.selectOrderList(
                    keyword, status, startDateStr, endDateStr, offset, size
            );
            log.debug("Found {} orders for page {}", result.size(), page);
            return result;
        } catch (Exception e) {
            log.error("Error occurred while fetching paged orders", e);
            throw new RuntimeException("주문 목록 조회 중 오류가 발생했습니다. 관리자에게 문의하세요.", e);
        }
    }

    @Override
    public int getOrderCount(
            String keyword,
            String status,
            LocalDate startDate,
            LocalDate endDate
    ) {
        String startDateStr = (startDate != null) ? startDate.toString() : null;
        String endDateStr   = (endDate != null)   ? endDate.toString()   : null;

        log.debug("Counting orders with parameters: keyword={}, status={}, startDate={}, endDate={}",
                keyword, status, startDateStr, endDateStr);

        try {
            return orderMapper.orderCount(keyword, status, startDateStr, endDateStr);
        } catch (Exception e) {
            log.error("Error occurred while counting orders", e);
            throw new RuntimeException("주문 개수 조회 중 오류가 발생했습니다. 관리자에게 문의하세요.", e);
        }
    }

    /**
     * 가출고 처리를 수행하고, 
     *  - 해당 ORD_DETAIL 한 건을 STATUS='완료' 로 업데이트
     *  - 같은 ORD_HEADER 내 미완료(진행중) ORD_DETAIL 건이 있으면 ORD_HEADER 상태를 '진행중'으로,
     *    없으면(모두 이미 완료된 경우) ORD_HEADER 상태를 '완료'로 업데이트
     */
    @Override
    @Transactional
    public void processProvisionalShipment(OrderDto.ProvisionalShipmentRequest request) throws Exception {
        // 1) 현재 ORD_DETAIL 상태 조회
        String currentStatus = orderMapper.selectOrderDetailStatus(request.getOrderDetailId());
        if ("완료".equals(currentStatus) || "취소".equals(currentStatus)) {
            throw new IllegalStateException("이미 처리된 주문상세ID(" + request.getOrderDetailId() + ") 입니다.");
        }

        // 2) 기존 로직: INOUT 삽입
        int insertResult = orderMapper.insertProvisionalShipmentToInOut(request);
        if (insertResult == 0) {
            throw new Exception("가출고 내역 저장 실패");
        }

        // 3) ORD_DETAIL 상태를 “완료”로 변경
        int detailUpdate = orderMapper.updateOrderDetailStatusToCompleted(request.getOrderDetailId());
        if (detailUpdate == 0) {
            throw new Exception("주문상세 상태 변경 실패");
        }

        // 4) ORD_HEADER 상태 결정: “완료”가 될지 “진행중”이 될지 판단
        //    같은 ORD_ID의 나머지 ORD_DETAIL 중에 아직 “완료”가 아닌 항목이 있으면 “진행중”, 
        //    모두 “완료”면 “완료”로 상태 변경.
        Long ordId = request.getOrdId();
        boolean allCompleted = orderMapper.countRemainingOrderDetail(ordId) == 0;
        if (allCompleted) {
            orderMapper.updateOrderHeaderStatusToCompleted(ordId);
        } else {
            orderMapper.updateOrderHeaderStatusToInProgress(ordId);
        }
    }
    
    @Override
    public String getOrderHeaderStatus(Long ordId) {
        return orderMapper.selectOrderHeaderStatus(ordId);
    }

}
