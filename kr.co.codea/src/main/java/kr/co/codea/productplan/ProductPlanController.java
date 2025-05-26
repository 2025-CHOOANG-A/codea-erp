package kr.co.codea.productplan;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequestMapping("/productplan")
public class ProductPlanController {
    private static final Logger log = LoggerFactory.getLogger(ProductPlanController.class); // 로거 추가

	private ProductPlanService service;
	
	public ProductPlanController(ProductPlanService service) {
		this.service = service;
	}
	
	//품목검색 api
	@GetMapping("/api/item/search")
	@ResponseBody
	public ResponseEntity<List<ProductPlanDTO>> searchItem(@RequestParam(value = "query", required = false) String query){
		List<ProductPlanDTO> searchItem = service.searchItem(query);
		
		return ResponseEntity.ok(searchItem);
	}
	
	//생산계획 리스트
	@GetMapping
	public String productPlanList(Model m, ProductPlanDTO dto) {
		List<ProductPlanDTO> list = service.ProductPlanList(dto);
        m.addAttribute("productionPlans", list);
        m.addAttribute("searchDto", dto); 
		
        m.addAttribute("templateName", "productplan/productplan_list");
        m.addAttribute("fragmentName", "contentFragment");
        
		return "productplan/productplan_default";
	}
	
	//생산계획 생성
	@PostMapping("/write")
	public ResponseEntity<Map<String, Object>> productPlanWrite(@RequestBody ProductPlanDTO dto){
		Map<String, Object> response = new HashMap<>();
		
		try {
			
			int result = service.insertProductPlan(dto);
			if (result > 0) {
				response.put("success", true);
				response.put("message", "생산 계획 등록이 완료되었습니다.");
			} else {
				response.put("success", false);
				response.put("message", "생산 계획 등록에 실패했습니다..");
			}


			return ResponseEntity.ok(response); //ResponseEntity.status(HttpStatus.OK) 와 동일
		} catch (Exception e) {
			 log.error("생산 계획 등록 중 오류 발생: {}", e.getMessage(), e); // 
			response.put("success", false);
			response.put("message", "서버 에러");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		
	}
	
	
	}
}