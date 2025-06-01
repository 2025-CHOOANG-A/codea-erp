package kr.co.codea.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

/**
 * 주문 관련 웹 요청을 처리하는 컨트롤러
 */
@Slf4j
@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 주문 목록 페이지를 조회합니다.
     * 
     * @param keyword 검색 키워드 (거래처명 또는 제품명)
     * @param status 주문 상태 (진행중, 완료 등)
     * @param startDate 검색 시작일
     * @param endDate 검색 종료일
     * @param page 페이지 번호 (0부터 시작)
     * @param model 뷰에 전달할 모델 데이터
     * @return 주문 목록 뷰 이름
     */
    @GetMapping
    public String getOrderList(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "startDate", required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(value = "endDate", required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model
    ) {
        log.info("Order list requested - keyword: {}, status: {}, startDate: {}, endDate: {}, page: {}", 
                keyword, status, startDate, endDate, page);
        
        try {
            // 페이지 크기 설정
            int size = 10;
            
            // 검색 조건 유효성 검증
            if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
                log.warn("Invalid date range: startDate {} is after endDate {}", startDate, endDate);
                model.addAttribute("errorMessage", "시작일이 종료일보다 늦을 수 없습니다.");
                // 날짜 조건을 무시하고 조회
                startDate = null;
                endDate = null;
            }
            
            // 주문 목록 조회
            List<OrderDto.OrderDetailView> content = orderService.getPagedOrders(
                    keyword, status, startDate, endDate, page, size
            );
            
            // 전체 개수 조회
            int totalCount = orderService.getOrderCount(keyword, status, startDate, endDate);
            int totalPages = (int) Math.ceil((double) totalCount / size);
            
            // 페이지 번호 유효성 검증
            if (page >= totalPages && totalPages > 0) {
                log.warn("Invalid page number: {}. Total pages: {}. Redirecting to last page.", page, totalPages);
                return "redirect:/order?page=" + (totalPages - 1) + 
                       buildQueryString(keyword, status, startDate, endDate);
            }
            
            // 모델에 데이터 추가
            model.addAttribute("pagedOrderDetails", 
                    new OrderDto.PagingResult<>(content, page, totalPages));
            model.addAttribute("param", 
                    new OrderDto.SearchParam(keyword, status, startDate, endDate));
            
            // 검색 결과 통계
            model.addAttribute("totalCount", totalCount);
            model.addAttribute("currentPageSize", content.size());
            
            log.info("Order list loaded successfully - {} orders found, page {}/{}", 
                    totalCount, page + 1, totalPages);
            
            return "order/order_list";
            
        } catch (Exception e) {
            log.error("Error occurred while loading order list", e);
            model.addAttribute("errorMessage", "주문 목록을 불러오는 중 오류가 발생했습니다.");
            model.addAttribute("pagedOrderDetails", 
                    new OrderDto.PagingResult<>(List.of(), 0, 0));
            model.addAttribute("param", 
                    new OrderDto.SearchParam(keyword, status, startDate, endDate));
            return "order/order_list";
        }
    }

    /**
     * 주문 등록 페이지로 이동합니다.
     * 
     * @return 주문 등록 뷰 이름
     */
    @GetMapping("/register")
    public String registerOrderForm(Model model) {
        log.info("Order registration form requested");
        
        // TODO: 주문 등록에 필요한 데이터 설정 (거래처 목록, 제품 목록 등)
        // model.addAttribute("businessPartners", businessPartnerService.getAllBusinessPartners());
        // model.addAttribute("products", productService.getAllProducts());
        
        return "order/order_register";
    }

    /**
     * 쿼리 스트링을 생성하는 헬퍼 메서드
     * 
     * @param keyword 검색 키워드
     * @param status 주문 상태
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 쿼리 스트링
     */
    private String buildQueryString(String keyword, String status, LocalDate startDate, LocalDate endDate) {
        StringBuilder query = new StringBuilder();
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            query.append("&keyword=").append(keyword);
        }
        if (status != null && !status.trim().isEmpty()) {
            query.append("&status=").append(status);
        }
        if (startDate != null) {
            query.append("&startDate=").append(startDate);
        }
        if (endDate != null) {
            query.append("&endDate=").append(endDate);
        }
        
        return query.toString();
    }
}