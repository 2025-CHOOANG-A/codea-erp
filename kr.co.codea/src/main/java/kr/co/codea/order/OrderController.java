package kr.co.codea.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // 기본 경로가 리스트 조회
    @GetMapping
    public String getOrderList(
    		@RequestParam(name = "keyword", required = false) String keyword,
    	    @RequestParam(name = "status", required = false) String status,
    	    @RequestParam(name = "startDate", required = false) String startDate,
    	    @RequestParam(name = "endDate", required = false) String endDate,
    	    @RequestParam(name = "page", defaultValue = "1") int page,
            Model model
    ) {
        int pageSize = 10;
        int offset = (page - 1) * pageSize;

        Map<String, Object> params = new HashMap<>();
        params.put("keyword", keyword);
        params.put("status", status);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("offset", offset);
        params.put("limit", pageSize);

        List<OrderDto> orderList = orderService.getOrderList(params);
        int totalCount = orderService.getOrderCount(params);
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);

        int startPage = Math.max(1, page - 2);
        int endPage = Math.min(totalPages, page + 2);

        model.addAttribute("orderList", orderList);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("params", params);

        return "order/order_list"; // templates/order/order_list.html
    }
}
