package kr.co.codea.order;

import java.time.LocalDate;
import java.util.List;

/**
 * 주문 관련 비즈니스 로직을 처리하는 서비스 인터페이스
 */
public interface OrderService {

    /**
     * 페이징된 주문 목록을 조회합니다.
     *
     * @param keyword 검색 키워드 (거래처명 또는 제품명, null 허용)
     * @param status 주문 상태 (진행중, 완료 등, null 허용)
     * @param startDate 검색 시작일 (null 허용)
     * @param endDate 검색 종료일 (null 허용)
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지당 항목 수
     * @return 주문 상세 정보 리스트
     */
    List<OrderDto.OrderDetailView> getPagedOrders(
            String keyword,
            String status,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size
    );

    /**
     * 검색 조건에 맞는 주문의 총 개수를 조회합니다.
     *
     * @param keyword 검색 키워드 (거래처명 또는 제품명, null 허용)
     * @param status 주문 상태 (진행중, 완료 등, null 허용)
     * @param startDate 검색 시작일 (null 허용)
     * @param endDate 검색 종료일 (null 허용)
     * @return 조건에 맞는 주문 총 개수
     */
    int getOrderCount(
            String keyword,
            String status,
            LocalDate startDate,
            LocalDate endDate
    );

    /**
     * 가출고 처리를 수행합니다.
     * <p>
     * 1. INOUT 테이블에 가출고 내역을 기록합니다.
     * 2. 해당 주문(ORD_HEADER)의 상태를 '완료'로 변경합니다.
     * </p>
     *
     * @param request 가출고 요청 정보를 담은 DTO
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    void processProvisionalShipment(OrderDto.ProvisionalShipmentRequest request) throws Exception;
}