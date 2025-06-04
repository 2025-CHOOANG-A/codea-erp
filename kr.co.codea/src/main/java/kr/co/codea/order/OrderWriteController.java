package kr.co.codea.order;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderWriteController {

    private final OrderWriteService writeService;

    /**
     * 주문 등록 화면으로 이동
     */
    @GetMapping("/register")
    public String showOrderRegisterForm(Model model) {
        log.info("주문 등록 화면 요청");
        OrderWriteDto.FormOrderCreateRequest emptyDto = new OrderWriteDto.FormOrderCreateRequest();
        model.addAttribute("orderForm", emptyDto);
        return "order/order_write";
    }

    /**
     * 주문 등록 처리 (헤더+상세 저장)
     */
    @PostMapping("/write")
    public String handleOrderWrite(
            @ModelAttribute("orderForm") OrderWriteDto.FormOrderCreateRequest formDto,
            Model model
    ) {
        log.info("주문 등록 처리 시작");
        log.info("주문 데이터 - orderDate: {}, bpId: {}, empId: {}, remark: '{}'", 
                 formDto.getOrderDate(), formDto.getBpId(), formDto.getEmpId(), formDto.getRemark());
        
        if (formDto.getDetailList() != null) {
            log.info("주문 상세 - 총 {}건", formDto.getDetailList().size());
            for (int i = 0; i < formDto.getDetailList().size(); i++) {
                var detail = formDto.getDetailList().get(i);
                log.info("상세[{}] - itemId: {}, qty: {}, price: {}, requiredDate: {}", 
                         i, detail.getItemId(), detail.getOrderQty(), 
                         detail.getUnitPrice(), detail.getRequiredDate());
            }
        } else {
            log.warn("주문 상세 데이터가 null입니다!");
        }
        
        try {
            writeService.createOrder(formDto);
            log.info("주문 등록 성공 - 목록으로 리다이렉트");
            return "redirect:/order";
        } catch (Exception e) {
            log.error("주문 등록 실패", e);
            model.addAttribute("errorMessage", "주문 등록 중 오류가 발생했습니다: " + e.getMessage());
            model.addAttribute("orderForm", formDto); // 폼 데이터 유지
            return "order/order_write";
        }
    }

    /**
     * [AJAX] 품목 자동완성 검색
     */
    @GetMapping("/items/search")
    @ResponseBody
    public List<OrderWriteDto.ItemSimple> searchItems(@RequestParam("keyword") String keyword) {
        log.debug("품목 검색 API 호출 - keyword: '{}'", keyword);
        return writeService.searchItems(keyword);
    }

    /**
     * [AJAX] 발주처(거래처) 자동완성 검색
     */
    @GetMapping("/partners/search")
    @ResponseBody
    public List<OrderWriteDto.PartnerSimple> searchPartners(@RequestParam("keyword") String keyword) {
        log.debug("발주처 검색 API 호출 - keyword: '{}'", keyword);
        return writeService.searchPartners(keyword);
    }

    /**
     * [AJAX] 담당자 자동완성 검색
     */
    @GetMapping("/employees/search")
    @ResponseBody
    public List<OrderWriteDto.EmployeeSimple> searchEmployees(@RequestParam("keyword") String keyword) {
        log.debug("담당자 검색 API 호출 - keyword: '{}'", keyword);
        return writeService.searchEmployees(keyword);
    }
}