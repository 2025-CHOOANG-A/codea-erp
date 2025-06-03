package kr.co.codea.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
// import kr.co.codea.common.utils.SecurityUtil; // 현재 로그인 사용자 ID를 가져오는 유틸리티 (가정)

/**
 * 주문 관련 비즈니스 로직을 처리하는 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
// 클래스 레벨 @Transactional(readOnly = true)는 읽기 전용 메서드에 기본 적용됩니다.
// 데이터 변경이 있는 메서드에는 별도로 @Transactional을 붙여야 합니다.
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    // private final WarehouseService warehouseService; // 창고 ID 결정 로직이 복잡할 경우 주입 (가정)

    /**
     * 페이징된 주문 목록을 조회합니다.
     */
    @Override
    public List<OrderDto.OrderDetailView> getPagedOrders(
            String keyword,
            String status,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size
    ) {
        // 페이지 번호 유효성 검증
        if (page < 0) {
            log.warn("Invalid page number: {}. Setting to 0.", page);
            page = 0;
        }

        // 페이지 크기 유효성 검증
        if (size <= 0) {
            log.warn("Invalid page size: {}. Setting to 10.", size);
            size = 10;
        }

        // LocalDate를 String으로 변환 (null-safe)
        String startDateStr = startDate != null ? startDate.toString() : null;
        String endDateStr = endDate != null ? endDate.toString() : null;

        // 오프셋 계산
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
            // 여기서 사용자 정의 예외를 던지거나, 컨트롤러에서 처리할 수 있도록 RuntimeException을 유지할 수 있습니다.
            throw new RuntimeException("주문 목록 조회 중 오류가 발생했습니다. 관리자에게 문의하세요.", e);
        }
    }

    /**
     * 검색 조건에 맞는 주문의 총 개수를 조회합니다.
     */
    @Override
    public int getOrderCount(
            String keyword,
            String status,
            LocalDate startDate,
            LocalDate endDate
    ) {
        // LocalDate를 String으로 변환 (null-safe)
        String startDateStr = startDate != null ? startDate.toString() : null;
        String endDateStr = endDate != null ? endDate.toString() : null;

        log.debug("Counting orders with parameters: keyword={}, status={}, startDate={}, endDate={}",
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

    /**
     * 가출고 처리를 수행하고 주문 상태를 '완료'로 변경합니다.
     * 이 메서드는 데이터 변경을 포함하므로 별도의 @Transactional 설정을 적용합니다.
     */
    @Override
    @Transactional // readOnly = false (기본값), 데이터 변경을 위한 트랜잭션 적용
    public void processProvisionalShipment(OrderDto.ProvisionalShipmentRequest request) throws Exception {
        log.info("가출고 처리 시작: OrderDetailID={}, ItemID={}, Quantity={}",
                request.getOrderDetailId(), request.getItemId(), request.getQuantity());

        // 1. EMP_ID 설정 (예: 현재 로그인한 사용자 ID)
        //    실제 환경에서는 Spring Security 등에서 사용자 정보를 가져와야 합니다.
        //    여기서는 예시로 하드코딩하거나, SecurityUtil 같은 유틸리티 클래스를 가정합니다.
        if (request.getEmpId() == null) {
            // Long currentUserId = SecurityUtil.getCurrentUserId(); // 예시: 현재 사용자 ID 가져오기
            // request.setEmpId(currentUserId);
            // 임시로 EMP_ID를 설정하지 않거나, 테스트용 ID를 넣을 수 있습니다.
            // request.setEmpId(1L); // 예시: 테스트용 직원 ID
            log.warn("EMP_ID가 설정되지 않았습니다. 가출고 요청 DTO에서 empId가 null입니다.");
        }
        
        // 2. WH_ID (창고 ID) 설정
        //    - 특정 품목의 기본 출고 창고를 설정하거나,
        //    - 주문 정보 또는 사용자 세션에서 창고 정보를 가져올 수 있습니다.
        //    - 여기서는 DTO에 whId가 필수로 전달된다고 가정하거나, 기본값을 설정합니다.
        if (request.getWhId() == null) {
            // request.setWhId(warehouseService.getDefaultWarehouseIdForItem(request.getItemId())); // 예시
            // 또는 필수 값으로 간주하고, Controller에서 검증하거나 여기서 예외 발생
            log.error("창고 ID(whId)가 없습니다. 가출고 처리를 위해 창고 ID는 필수입니다.");
            throw new IllegalArgumentException("가출고 처리를 위한 창고 ID(whId)가 누락되었습니다.");
        }

        // 3. INOUT 테이블에 가출고 내역 기록
        int insertResult = orderMapper.insertProvisionalShipmentToInOut(request);
        if (insertResult == 0) {
            log.error("INOUT 테이블에 가출고 내역 기록 실패: {}", request);
            throw new Exception("가출고 내역 저장에 실패했습니다. 다시 시도해주세요.");
        }
        log.info("INOUT 테이블에 가출고 내역 기록 성공: OrderDetailID={}", request.getOrderDetailId());

        // 4. ORD_HEADER의 상태를 '완료'로 변경
        //    주의: 한 주문에 여러 품목이 있고, 그 중 하나만 가출고 처리해도 주문 전체가 '완료'되는 로직입니다.
        //    만약 모든 품목이 가출고 처리되었을 때만 '완료'로 변경하려면 추가적인 로직이 필요합니다.
        //    (예: 해당 ORD_ID의 모든 ORD_DETAIL 항목들이 가출고 처리되었는지 확인)
        //    현재 요구사항은 '가출고 처리하면 주문 목록 화면에서는 완료로만 표시'이므로, 바로 업데이트합니다.
        int updateResult = orderMapper.updateOrderHeaderStatus(request.getOrdId());
        if (updateResult == 0) {
            log.error("ORD_HEADER 상태 '완료' 업데이트 실패: ORD_ID={}", request.getOrdId());
            // 이 경우 이미 INOUT에 기록된 내역을 어떻게 처리할지 결정해야 합니다 (롤백 등).
            // @Transactional에 의해 이 메서드 전체가 롤백될 것입니다.
            throw new Exception("주문 상태 변경에 실패했습니다. 다시 시도해주세요.");
        }
        log.info("ORD_HEADER 상태 '완료' 업데이트 성공: ORD_ID={}", request.getOrdId());

        log.info("가출고 처리 및 주문 완료 처리 성공: ORD_ID={}, OrderDetailID={}", request.getOrdId(), request.getOrderDetailId());
    }
}