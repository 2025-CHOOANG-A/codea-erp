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

@Slf4j
@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

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
        try {
            int size = 10;

            if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
                model.addAttribute("errorMessage", "시작일이 종료일보다 늦을 수 없습니다.");
                startDate = null;
                endDate = null;
            }

            List<OrderDto.OrderDetailView> content = orderService.getPagedOrders(
                    keyword, status, startDate, endDate, page, size
            );

            // 실시간 재고 계산하여 주입
            for (OrderDto.OrderDetailView dto : content) {
                if (dto.getItemId() != null) {
                    Integer stockQty = orderService.getRealInventoryQty(dto.getItemId());
                    dto.setStockQty(stockQty);
                }
            }

            int totalCount = orderService.getOrderCount(keyword, status, startDate, endDate);
            int totalPages = (totalCount == 0) ? 0 : (int) Math.ceil((double) totalCount / size);

            if (totalPages > 0 && page >= totalPages) {
                return "redirect:/order?page=" + (totalPages - 1) + buildQueryString(keyword, status, startDate, endDate);
            } else if (page < 0) {
                return "redirect:/order?page=0" + buildQueryString(keyword, status, startDate, endDate);
            }

            model.addAttribute("pagedOrderDetails", new OrderDto.PagingResult<>(content, page, totalPages));
            model.addAttribute("param", new OrderDto.SearchParam(keyword, status, startDate, endDate));
            model.addAttribute("totalCount", totalCount);
            model.addAttribute("currentPageSize", content.size());

            return "order/order_list";

        } catch (RuntimeException e) {
            log.error("Error loading order list: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "주문 목록을 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            model.addAttribute("pagedOrderDetails", new OrderDto.PagingResult<>(List.of(), 0, 0));
            model.addAttribute("param", new OrderDto.SearchParam(keyword, status, startDate, endDate));
            return "order/order_list";
        } catch (Exception e) {
            log.error("Unexpected error occurred while loading order list", e);
            model.addAttribute("errorMessage", "예상치 못한 오류로 주문 목록을 불러올 수 없습니다.");
            model.addAttribute("pagedOrderDetails", new OrderDto.PagingResult<>(List.of(), 0, 0));
            model.addAttribute("param", new OrderDto.SearchParam(keyword, status, startDate, endDate));
            return "order/order_list";
        }
    }


    @PostMapping("/api/shipment")
    @ResponseBody
    public ResponseEntity<?> handleProvisionalShipment(@RequestBody OrderDto.ProvisionalShipmentRequest request) {
        try {
            if (request.getWhId() == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "창고 ID(whId)가 필요합니다."));
            }

            orderService.processProvisionalShipment(request);
            return ResponseEntity.ok(Map.of("success", true, "message", "가출고 처리 및 주문 완료 처리가 성공적으로 완료되었습니다."));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "가출고 처리 중 오류: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "가출고 처리 중 예상치 못한 오류가 발생했습니다."));
        }
    }

    private String buildQueryString(String keyword, String status, LocalDate startDate, LocalDate endDate) {
        StringBuilder query = new StringBuilder();
        if (keyword != null && !keyword.isBlank()) {
            query.append("&keyword=").append(keyword);
        }
        if (status != null && !status.isBlank()) {
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
    
    @GetMapping("/{ordId}")
    public String getOrderDetail(@PathVariable("ordId") Long ordId, Model model) {
        try {
            OrderDto.OrderDetailPage order = orderService.getOrderDetail(ordId);
            if (order == null) {
                model.addAttribute("errorMessage", "해당 주문을 찾을 수 없습니다.");
                return "order/order_detail";
            }
            model.addAttribute("order", order);
            return "order/order_detail"; // templates/order/order_detail.html
        } catch (Exception e) {
            log.error("Error loading order detail: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "주문 상세를 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            return "order/order_detail";
        }
    }
}
