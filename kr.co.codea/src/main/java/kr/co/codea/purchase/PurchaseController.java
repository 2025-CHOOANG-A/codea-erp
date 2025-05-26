package kr.co.codea.purchase;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 구매관리 메인 컨트롤러
 */
@Controller
@RequestMapping("/purchase")
public class PurchaseController {

    // 구매관리 메인 화면 출력 (purchase/purchase_list.html 반환)
    @GetMapping
    public String showPurchaseList() {
        return "purchase/purchase_list";
    }
}
