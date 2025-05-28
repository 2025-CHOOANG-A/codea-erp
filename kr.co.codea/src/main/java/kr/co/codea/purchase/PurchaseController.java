package kr.co.codea.purchase;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

/**
 * 발주관리 메인 컨트롤러
 */
@Controller
@RequestMapping("/purchase")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;
    private static final int ITEMS_PER_PAGE = 10; // 페이지당 보여줄 항목 수
    private static final int PAGE_SIZE = 5;       // 하단에 보여줄 페이지 번호 개수

    // 구매관리 메인 화면 출력 (purchase/purchase_list.html 반환)
    @GetMapping
    public String getPurchaseList(@RequestParam Map<String, Object> params,
                                  @RequestParam(value = "page", defaultValue = "1") int currentPage, // 현재 페이지 번호
                                  Model model) {

        // 전체 아이템 개수 (검색 조건 포함)
        int totalCount = purchaseService.getPurchaseListCount(params);

        // 페이징 계산
        int totalPages = (int) Math.ceil((double) totalCount / ITEMS_PER_PAGE); // 전체 페이지 수
        int startPage = ((currentPage - 1) / PAGE_SIZE) * PAGE_SIZE + 1; // 현재 페이지 블록의 시작 페이지
        int endPage = Math.min(startPage + PAGE_SIZE - 1, totalPages);   // 현재 페이지 블록의 끝 페이지
        
        // 실제 쿼리에서 사용할 row 범위 계산 (Oracle ROWNUM 기준)
        // MyBatis 쿼리에서는 rn > #{startRowForQuery} AND ROWNUM <= #{endRowForQuery} 형태로 사용됩니다.
        int startRowOffset = (currentPage - 1) * ITEMS_PER_PAGE; 
        params.put("startRowForQuery", startRowOffset); // 건너뛸 row 개수 (0부터 시작)
        params.put("endRowForQuery", startRowOffset + ITEMS_PER_PAGE); // 가져올 마지막 row의 번호 (startRowOffset 기준 + 가져올 개수)

        List<PurchaseDto> purchaseList = purchaseService.getPurchaseList(params);

        model.addAttribute("purchaseList", purchaseList);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("params", params); // 검색 조건 유지를 위해 파라미터 전달

        // 페이징 관련 속성 추가
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("ITEMS_PER_PAGE", ITEMS_PER_PAGE); // 페이지당 항목 수
        model.addAttribute("PAGE_SIZE", PAGE_SIZE);           // 페이지네이션 바에 표시할 페이지 수
        
        // 이전/다음 페이지 그룹 존재 여부 및 이동할 페이지 번호
        model.addAttribute("hasPreviousPageGroup", startPage > 1);
        model.addAttribute("hasNextPageGroup", endPage < totalPages);
        model.addAttribute("previousPageGroupStart", Math.max(1, startPage - PAGE_SIZE)); // 이전 페이지 그룹의 시작 페이지
        model.addAttribute("nextPageGroupStart", Math.min(totalPages, endPage + 1));   // 다음 페이지 그룹의 시작 페이지


        return "purchase/purchase_list";
    }
    
 // 발주 상세 정보 조회
    @GetMapping("/{purchaseId}")
    public String getPurchaseDetail(@PathVariable("purchaseId") int purchaseId, Model model) {
        PurchaseDto purchase = purchaseService.getPurchaseDetail(purchaseId);
        if (purchase == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "발주 정보를 찾을 수 없습니다.");
        }
        model.addAttribute("purchase", purchase);
        return "purchase/purchase_detail";
    }
    
    @GetMapping("/create")
    public String showWriteForm(Model model) {
        // 오늘 날짜 기본값 전달 (Thymeleaf에서 사용 가능)
        model.addAttribute("today", java.time.LocalDate.now().toString());
        return "purchase/purchase_write";
    }
    
    @GetMapping("/items/search")
    @ResponseBody
    public List<PurchaseDto.ItemSimple> searchItems(@RequestParam("keyword") String keyword) {
        return purchaseService.searchItems(keyword);
    }

    @GetMapping("/suppliers/search")
    @ResponseBody
    public List<PurchaseDto.SupplierSimple> searchSuppliers(@RequestParam("keyword") String keyword) {
        return purchaseService.searchSuppliers(keyword);
    }

    @GetMapping("/employees/search")
    @ResponseBody
    public List<PurchaseDto.EmployeeSimple> searchEmployees(@RequestParam("keyword") String keyword) {
        return purchaseService.searchEmployees(keyword);
    }
    
    
}