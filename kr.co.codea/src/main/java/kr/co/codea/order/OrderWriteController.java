package kr.co.codea.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import kr.co.codea.order.OrderWriteDto.FormOrderCreateRequest;

/**
 * 주문 등록 페이지를 보여주고, 폼 제출을 처리하는 컨트롤러
 */
@Slf4j
@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderWriteController {

    private final OrderWriteService writeService;

    /**
     * GET  /order/register
     * 주문 등록 화면으로 이동
     */
    @GetMapping("/register")
    public String showOrderRegisterForm(Model model) {
        // 폼 바인딩용 빈 DTO 객체를 생성해서 뷰로 넘긴다
        FormOrderCreateRequest emptyDto = new FormOrderCreateRequest();
        model.addAttribute("orderForm", emptyDto);
        return "order/order_write";  // templates/order/order_register.html
    }

    /**
     * POST /order/write
     * 주문 등록 폼에서 넘어온 값을 바인딩하고, DB에 저장한 뒤 목록으로 리다이렉트
     */
    @PostMapping("/write")
    public String handleOrderWrite(
            @ModelAttribute("orderForm") FormOrderCreateRequest formDto,
            BindingResult bindingResult,
            Model model
    ) {
        // (1) 간단한 서버측 검증: 필수 필드 누락 체크
        if (formDto.getBpId() == null || formDto.getEmpId() == null) {
            model.addAttribute("errorMessage", "거래처 및 담당자는 반드시 선택해야 합니다.");
            return "order/order_write";
        }
        if (formDto.getDetailList() == null || formDto.getDetailList().isEmpty()) {
            model.addAttribute("errorMessage", "최소 한 개의 제품을 추가해야 합니다.");
            return "order/order_write";
        }
        for (int i = 0; i < formDto.getDetailList().size(); i++) {
            var detail = formDto.getDetailList().get(i);
            if (detail.getItemId() == null
                    || detail.getOrderQty() == null
                    || detail.getRequiredDate() == null) {
                model.addAttribute("errorMessage", (i + 1) + "번째 행의 필드를 모두 채워주세요.");
                return "order/order_write";
            }
        }

        try {
            // (2) 서비스 호출 → ORD_HEADER + ORD_DETAIL 모두 저장
            writeService.createOrder(formDto);
            // 저장 성공 시, 목록 페이지로 리다이렉트
            return "redirect:/order";
        } catch (Exception e) {
            log.error("주문 등록 중 오류", e);
            model.addAttribute("errorMessage", "주문 등록 중 오류가 발생했습니다: " + e.getMessage());
            return "order/order_write";
        }
    }
}
