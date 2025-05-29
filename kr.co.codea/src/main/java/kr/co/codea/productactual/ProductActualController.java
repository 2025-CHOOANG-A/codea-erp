package kr.co.codea.productactual;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping("/productactual")
public class ProductActualController {
	private ProductActualService service;
	
	public ProductActualController(ProductActualService service) {
		this.service = service;
	}
	
	@GetMapping
	public String productActualList(Model m, ProductActualDTO dto) {
		List<ProductActualDTO> list = service.productActualList(dto);

		
        m.addAttribute("productionPlans", list);
        m.addAttribute("searchDto", dto); 
		
        m.addAttribute("templateName", "productactual/productactual_list");
        m.addAttribute("fragmentName", "contentFragment");
        
		return "productactual/productactual_default";
		
	}
	
    @PostMapping("/insert")
    @ResponseBody
    public ResponseEntity<?> registerProductActual(@RequestBody ProductActualDTO dto) {
        try {
            service.registerProductActual(dto); // 서비스 계층에서 실적 등록 로직 처리
            return ResponseEntity.ok(Map.of("message", "생산 실적이 성공적으로 등록되었습니다."));
        } catch (IllegalArgumentException e) {
            // 유효성 검사 실패 등 클라이언트 오류
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            // 그 외 서버 오류
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "실적 등록에 실패했습니다: " + e.getMessage()));
        }
    }
    
    
    
    @GetMapping("/daily")
    @ResponseBody 
    public ResponseEntity<List<ProductActualDTO>> getDailyActualsByPlanId(@RequestParam("planId") String planId) {
        List<ProductActualDTO> dailyActuals = service.getDailyActualsByPlanId(planId);
        return ResponseEntity.ok(dailyActuals);
    }
	

}
