package kr.co.codea.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // 기본적으로 조회 전용, 데이터 변경 메서드에 별도 @Transactional 적용
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
            log.debug("Found {} order-detail items for page {}", result.size(), page);
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
    @Transactional  // INSERT/UPDATE 작업을 위해 트랜잭션 적용
    public void processProvisionalShipment(OrderDto.ProvisionalShipmentRequest request) throws Exception {
        log.info("가출고 처리 시작: OrderDetailID={}, ItemID={}, Quantity={}",
                request.getOrderDetailId(), request.getItemId(), request.getQuantity());

        // 1) 창고 ID(whId) 필수 검증
        if (request.getWhId() == null) {
            log.error("창고 ID(whId)가 없습니다. 가출고 처리를 위해 whId는 필수입니다.");
            throw new IllegalArgumentException("가출고 처리를 위한 창고 ID(whId)가 누락되었습니다.");
        }

        // 2) ITEM 테이블에서 PRICE 조회
        BigDecimal price = orderMapper.selectItemPrice(request.getItemId());
        log.debug("selectItemPrice 호출 결과 price = {}", price);
        if (price == null) {
            log.error("해당 Item ID의 가격을 찾을 수 없습니다. ItemID={}", request.getItemId());
            throw new IllegalArgumentException("해당 품목의 가격 정보를 찾을 수 없습니다.");
        }

        // 3) 조회된 price를 DTO에 세팅
        request.setItemUnitCost(price);
        log.debug("setItemUnitCost 호출 이후 request.getItemUnitCost() = {}", request.getItemUnitCost());

        // 4) INOUT 테이블에 가출고 정보 삽입
        int insertResult = orderMapper.insertProvisionalShipmentToInOut(request);
        if (insertResult == 0) {
            log.error("INOUT 테이블에 가출고 내역 기록 실패: {}", request);
            throw new Exception("가출고 내역 저장에 실패했습니다. 다시 시도해주세요.");
        }
        log.info("INOUT 테이블에 가출고 내역 기록 성공: OrderDetailID={}, ItemUnitCost={}",
                request.getOrderDetailId(), request.getItemUnitCost());

        // 5) ORD_DETAIL 상태를 '완료'로 변경
        int detailUpdateCount = orderMapper.updateOrderDetailStatus(request.getOrderDetailId());
        if (detailUpdateCount == 0) {
            log.error("ORD_DETAIL 상태 '완료' 업데이트 실패: ORD_DETAIL_ID={}", request.getOrderDetailId());
            throw new Exception("주문 상세 상태 변경에 실패했습니다.");
        }
        log.info("ORD_DETAIL 상태 '완료' 업데이트 성공: ORD_DETAIL_ID={}", request.getOrderDetailId());

        // 6) 남은 미완료 ORD_DETAIL 수 확인
        int pendingCount = orderMapper.countPendingDetail(request.getOrdId());
        if (pendingCount == 0) {
            // 7-1) 하나도 남아있지 않으면 ORD_HEADER 상태를 '완료'로 변경
            int headerUpdateCount = orderMapper.updateOrderHeaderStatus(request.getOrdId());
            if (headerUpdateCount == 0) {
                log.error("ORD_HEADER 상태 '완료' 업데이트 실패: ORD_ID={}", request.getOrdId());
                throw new Exception("주문 헤더 상태 변경에 실패했습니다.");
            }
            log.info("ORD_HEADER 상태 '완료' 업데이트 성공: ORD_ID={}", request.getOrdId());
        } else {
            log.info("아직 출고되지 않은 ORD_DETAIL({})이 존재하여 ORD_HEADER 상태를 변경하지 않습니다.", pendingCount);
        }

        log.info("가출고 처리 완료: ORD_ID={}, OrderDetailID={}", request.getOrdId(), request.getOrderDetailId());
    }
    
    @Override
    public int getRealInventoryQty(Long itemId) {
        if (itemId == null) return 0;
        Integer qty = orderMapper.selectRealInventoryQty(itemId.intValue());
        return qty != null ? qty : 0;
    }
}
