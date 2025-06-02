package kr.co.codea.order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;
    private final int DEFAULT_PAGE_SIZE = 10;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 1) 주문 목록 화면
     *    GET /order?page={page}&status={status}&keyword={keyword}&startDate={startDate}&endDate={endDate}
     */
    @GetMapping
    public String orderList(
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "status", required = false, defaultValue = "") String status,
            @RequestParam(name = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(name = "startDate", required = false)
                @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(name = "endDate", required = false)
                @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            Model model
    ) {
        // 1. 문자열 파라미터로 변환 (MyBatis XML에서 TO_DATE로 사용)
        String startDateStr = (startDate != null) ? startDate.toString() : "";
        String endDateStr   = (endDate   != null) ? endDate.toString()   : "";

        // 2. 서비스 호출
        OrderDto.PagingResult<OrderDto.OrderDetailView> pagedResult =
                orderService.getOrderList(keyword, status, startDateStr, endDateStr, page, DEFAULT_PAGE_SIZE);

        // 3. Model에 담아 템플릿에 전달
        model.addAttribute("pagedOrderDetails", pagedResult);
        model.addAttribute("param",       Map.of(
                "keyword", keyword,
                "status",  status,
                "startDate", startDateStr,
                "endDate",   endDateStr
        ));
        model.addAttribute("totalCount",      pagedResult.getTotalCount());
        model.addAttribute("currentPageSize", pagedResult.getCurrentPageSize());

        return "order/order_list";  // templates/order/order_list.html
    }

    /**
     * 2) 주문 상세 보기 페이지
     *    GET /order/detail/{ordId}
     */
    @GetMapping("/detail/{ordId}")
    public String orderDetail(@PathVariable("ordId") Long ordId, Model model) {
        // 1) 헤더 정보 조회
        OrderDto.OrderHeaderDetail header = orderService.getOrderHeaderDetail(ordId);
        if (header == null) {
            // 해당 주문이 없으면 목록으로 리다이렉트
            return "redirect:/order";
        }

        // 2) 상세 아이템 목록 조회
        List<OrderDto.OrderItemDetail> items = orderService.getOrderItems(ordId);
        header.setItems(items);

        // 3) 모델에 담아서 템플릿 호출
        model.addAttribute("order", header);
        return "order/order_detail";  // templates/order/order_detail.html
    }

    /**
     * 3) 가출고 처리 API (AJAX 호출)
     *    POST /order/api/shipment
     *    JSON 형태로 결과 반환 { success: true/false, headerStatus: "진행중" or "완료", message: "..." }
     */
    @PostMapping("/api/shipment")
    @ResponseBody
    public Map<String, Object> apiShipment(@RequestBody OrderDto.ProvisionalShipmentRequest request) {
        boolean ok = orderService.processShipment(request);
        Map<String, Object> response = new HashMap<>();
        if (ok) {
            // 처리 후 헤더 상태 조회
            String headerStatus = orderService
                    .getOrderHeaderDetail(request.getOrdId())
                    .getStatus();
            response.put("success", true);
            response.put("headerStatus", headerStatus);
        } else {
            response.put("success", false);
            response.put("message", "출고 처리에 실패했습니다.");
        }
        return response;
    }
}
