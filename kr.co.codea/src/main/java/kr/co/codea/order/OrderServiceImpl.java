package kr.co.codea.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 주문 관련 비즈니스 로직을 처리하는 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;

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
            
            log.debug("Found {} orders", result.size());
            return result;
            
        } catch (Exception e) {
            log.error("Error occurred while fetching paged orders", e);
            throw new RuntimeException("주문 목록 조회 중 오류가 발생했습니다.", e);
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
            throw new RuntimeException("주문 개수 조회 중 오류가 발생했습니다.", e);
        }
    }
}