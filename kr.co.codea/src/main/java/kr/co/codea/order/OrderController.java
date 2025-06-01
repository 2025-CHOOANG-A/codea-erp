package kr.co.codea.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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
            int size = 10;

            // 날짜 조건 유효성 검증
            if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
                log.warn("Invalid date range: startDate {} is after endDate {}", startDate, endDate);
                model.addAttribute("errorMessage", "시작일이 종료일보다 늦을 수 없습니다.");
                startDate = null;
                endDate = null;
            }

            // 서비스 호출
            List<OrderDto.OrderDetailView> content = orderService.getPagedOrders(
                    keyword, status, startDate, endDate, page, size
            );
            int totalCount = orderService.getOrderCount(keyword, status, startDate, endDate);
            int totalPages = (totalCount == 0) ? 0 : (int) Math.ceil((double) totalCount / size);

            if (totalPages > 0 && page >= totalPages) {
                log.warn("Invalid page number: {}. Redirecting to last page.", page);
                return "redirect:/order?page=" + (totalPages - 1)
                        + buildQueryString(keyword, status, startDate, endDate);
            } else if (page < 0) {
                log.warn("Invalid page number: {}. Redirecting to first page.", page);
                return "redirect:/order?page=0" 
                        + buildQueryString(keyword, status, startDate, endDate);
            }

            model.addAttribute("pagedOrderDetails",
                    new OrderDto.PagingResult<>(content, page, totalPages));
            model.addAttribute("param",
                    new OrderDto.SearchParam(keyword, status, startDate, endDate));
            model.addAttribute("totalCount", totalCount);
            model.addAttribute("currentPageSize", content.size());

            log.info("Order list loaded successfully - {} items found, page {}/{} (Total items: {})",
                    content.size(), page + 1, totalPages == 0 ? 1 : totalPages, totalCount);

            return "order/order_list"; 
        } catch (RuntimeException e) {
            log.error("Error occurred while loading order list: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "주문 목록을 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            model.addAttribute("pagedOrderDetails",
                    new OrderDto.PagingResult<>(List.of(), 0, 0));
            model.addAttribute("param",
                    new OrderDto.SearchParam(keyword, status, startDate, endDate));
            return "order/order_list";
        } catch (Exception e) {
            log.error("Unexpected error occurred while loading order list", e);
            model.addAttribute("errorMessage", "예상치 못한 오류로 주문 목록을 불러올 수 없습니다. 관리자에게 문의하세요.");
            model.addAttribute("pagedOrderDetails",
                    new OrderDto.PagingResult<>(List.of(), 0, 0));
            model.addAttribute("param",
                    new OrderDto.SearchParam(keyword, status, startDate, endDate));
            return "order/order_list";
        }
    }

    /**
     * 주문 등록 페이지로 이동합니다.
     */
    @GetMapping("/register")
    public String registerOrderForm(Model model) {
        log.info("Order registration form requested");
        return "order/order_register";
    }

    /**
     * 가출고 처리를 위한 API 엔드포인트입니다.
     */
    @PostMapping("/api/shipment")
    @ResponseBody
    public ResponseEntity<?> handleProvisionalShipment(@RequestBody OrderDto.ProvisionalShipmentRequest request) {
        try {
            orderService.processProvisionalShipment(request);

            // 새로 계산된 헤더 상태를 다시 조회 (진행중인지 완료인지)
            String newHeaderStatus = orderService.getOrderHeaderStatus(request.getOrdId());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "가출고 처리 및 주문 상태 변경 완료",
                "headerStatus", newHeaderStatus
            ));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Map.of("success", false, "message", "서버 오류: " + e.getMessage()));
        }
    }


    /**
     * 쿼리 스트링을 생성하는 헬퍼 메서드
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
            query.append("&startDate=").append(startDate.toString());
        }
        if (endDate != null) {
            query.append("&endDate=").append(endDate.toString());
        }
        return query.toString();
    }
}
