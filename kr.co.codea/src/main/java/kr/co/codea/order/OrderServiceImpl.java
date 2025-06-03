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
        String endDateStr = (endDate != null) ? endDate.toString() : null;
        int offset = page * size;

        log.debug("Searching orders with params keyword={}, status={}, startDate={}, endDate={}, page={}, size={}",
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
        String endDateStr = (endDate != null) ? endDate.toString() : null;

        log.debug("Counting orders with params keyword={}, status={}, startDate={}, endDate={}",
                keyword, status, startDateStr, endDateStr);

        try {
            int count = orderMapper.orderCount(keyword, status, startDateStr, endDateStr);
            log.debug("Total order count: {}", count);
            return count;
        } catch (Exception e) {
            log.error("Error occurred while counting orders", e);
            throw new RuntimeException("주문 개수 조회 중 오류가 발생했습니다. 관리자에게 문의하세요.", e);
        }
    }

    @Override
    @Transactional  // 데이터 변경을 위해 트랜잭션 적용
    public void processProvisionalShipment(OrderDto.ProvisionalShipmentRequest request) throws Exception {
        log.info("가출고 처리 시작: OrderDetailID={}, ItemID={}, Quantity={}",
                request.getOrderDetailId(), request.getItemId(), request.getQuantity());

        // 1) 창고 ID(whId) 필수 검증
        if (request.getWhId() == null) {
            log.error("창고 ID(whId)가 없습니다. 가출고 처리를 위해 whId는 필수입니다.");
            throw new IllegalArgumentException("가출고 처리를 위한 창고 ID(whId)가 누락되었습니다.");
        }
        // (필요하다면 EMP_ID 검증/셋팅도 여기에 추가)

        // 2) INOUT 테이블에 가출고 정보 삽입
        int insertResult = orderMapper.insertProvisionalShipmentToInOut(request);
        if (insertResult == 0) {
            log.error("INOUT 테이블에 가출고 내역 기록 실패: {}", request);
            throw new Exception("가출고 내역 저장에 실패했습니다. 다시 시도해주세요.");
        }
        log.info("INOUT 테이블에 가출고 내역 기록 성공: OrderDetailID={}", request.getOrderDetailId());

        // 3) 해당 ORD_DETAIL 상태를 '완료'로 변경
        Long ordDetailId = request.getOrderDetailId();
        int detailUpdateResult = orderMapper.updateOrderDetailStatus(ordDetailId);
        if (detailUpdateResult == 0) {
            log.error("ORD_DETAIL 상태 업데이트 실패: ORD_DETAIL_ID={}", ordDetailId);
            throw new Exception("주문 상세 상태 변경에 실패했습니다. 다시 시도해주세요.");
        }
        log.info("ORD_DETAIL 상태 '완료' 업데이트 성공: ORD_DETAIL_ID={}", ordDetailId);

        // 4) 남아 있는 미완료 ORD_DETAIL 건 수 조회
        Long ordId = request.getOrdId();
        int remaining = orderMapper.countPendingDetail(ordId);
        log.debug("ORD_HEADER({}) 기준 남은 미완료 ORD_DETAIL 수: {}", ordId, remaining);

        // 5) 남은 디테일이 0개일 때만 ORD_HEADER 상태를 '완료'로 변경
        if (remaining == 0) {
            int headerUpdateResult = orderMapper.updateOrderHeaderStatus(ordId);
            if (headerUpdateResult == 0) {
                log.error("ORD_HEADER 상태 '완료' 업데이트 실패: ORD_ID={}", ordId);
                throw new Exception("주문 헤더 상태 변경에 실패했습니다. 다시 시도해주세요.");
            }
            log.info("ORD_HEADER 상태 '완료' 업데이트 성공: ORD_ID={}", ordId);
        } else {
            log.info("아직 출고되지 않은 ORD_DETAIL({})이 존재하여 ORD_HEADER 상태를 변경하지 않습니다.", remaining);
        }

        log.info("가출고 처리 완료: ORD_ID={}, OrderDetailID={}", ordId, ordDetailId);
    }
}
