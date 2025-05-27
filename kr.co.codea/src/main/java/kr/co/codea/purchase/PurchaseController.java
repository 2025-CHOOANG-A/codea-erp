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
    private static final int ITEMS_PER_PAGE = 10; // 페이지당 보여줄 항목 수
    private static final int PAGE_SIZE = 5;      // 하단에 보여줄 페이지 번호 개수

    // 구매관리 메인 화면 출력 (purchase/purchase_list.html 반환)
    @GetMapping
    public String getPurchaseList(@RequestParam Map<String, Object> params, @RequestParam(value = "page", defaultValue = "1") int currentPage, Model model) {
        List<PurchaseDto> purchaseList = purchaseService.getPurchaseList(params);
        int totalCount = purchaseService.getPurchaseListCount(params);

        model.addAttribute("purchaseList", purchaseList);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("params", params); // 검색 조건 유지

        return "purchase/purchase_list";
    }
}
