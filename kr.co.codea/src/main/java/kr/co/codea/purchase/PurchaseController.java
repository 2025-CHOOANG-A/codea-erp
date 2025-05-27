package kr.co.codea.purchase;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;

/**
 * 구매관리 메인 컨트롤러
 */
@Controller
@RequestMapping("/purchase")
@RequiredArgsConstructor
public class PurchaseController {
	
	private final PurchaseService purchaseService;

    // 구매관리 메인 화면 출력 (purchase/purchase_list.html 반환)
    @GetMapping
    public String getPurchaseList(@RequestParam Map<String, Object> params, Model model) {
        List<PurchaseDto> purchaseList = purchaseService.getPurchaseList(params);
        int totalCount = purchaseService.getPurchaseListCount(params);

        model.addAttribute("purchaseList", purchaseList);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("params", params); // 검색 조건 유지

        return "purchase/purchase_list";
    }
}
