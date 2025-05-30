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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.validation.Valid;


@Controller
@RequestMapping("/productplan")
public class ProductPlanController {
    private static final Logger log = LoggerFactory.getLogger(ProductPlanController.class); // 로거 추가

	private ProductPlanService service;
	
	public ProductPlanController(ProductPlanService service) {
		this.service = service;
	}
	
	//작업지시 취소
	@PostMapping("/api/cancel-work-orders")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> cancelWorkOrders(@RequestBody ProductPlanDTO dto) {
	    Map<String, Object> response = new HashMap<>();
	    if (dto.getPlanIds() == null || dto.getPlanIds().isEmpty()) {
	        response.put("success", false);
	        response.put("message", "선택된 생산 계획이 없습니다.");
	        return ResponseEntity.badRequest().body(response);
	    }

	    try {
	        String mrpok = "작업지시";
	        String targetStatus = "자재계획완료";     

	        int updatedCount = service.changePlansStatus(dto.getPlanIds(), mrpok, targetStatus);

	        if (updatedCount > 0) {
	            response.put("success", true);
	            response.put("message", updatedCount + "개의 생산 계획에 대한 작업 지시가 취소되었습니다.");
	            return ResponseEntity.ok(response);
	        } else {
	            response.put("success", false);
	            response.put("message", "상태를 변경할 수 있는 생산 계획이 없거나, 이미 처리되었을 수 있습니다. (조건: " + mrpok + ")");

	            return ResponseEntity.status(HttpStatus.OK).body(response); //
	        }
	        
	        
	    } catch (IllegalArgumentException e) {
	        log.warn("작업 지시 생성 중 유효성 검사 오류: {}", e.getMessage());
	        response.put("success", false);
	        response.put("message", e.getMessage());
	        return ResponseEntity.badRequest().body(response);
	    } catch (Exception e) {
	        log.error("작업 지시 생성 중 서버 오류 발생: {}", e.getMessage(), e);
	        response.put("success", false);
	        response.put("message", "작업 지시 생성 중 서버 오류가 발생했습니다.");
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }
	}
	
	
	
	//작업지시생성
	@PostMapping("/api/issue-work-orders") //
	@ResponseBody 
	public ResponseEntity<Map<String, Object>> issueWorkOrders(@RequestBody ProductPlanDTO dto) {
	    Map<String, Object> response = new HashMap<>();
	    if (dto.getPlanIds() == null || dto.getPlanIds().isEmpty()) {
	        response.put("success", false);
	        response.put("message", "선택된 생산 계획이 없습니다.");
	        return ResponseEntity.badRequest().body(response);
	    }

	    try {
	        String mrpok = "자재계획완료";
	        String targetStatus = "작업지시";     

	        int updatedCount = service.changePlansStatus(dto.getPlanIds(), mrpok, targetStatus);

	        if (updatedCount > 0) {
	            response.put("success", true);
	            response.put("message", updatedCount + "개의 생산 계획에 대한 작업 지시가 성공적으로 생성되었습니다.");
	            return ResponseEntity.ok(response);
	        } else {
	            response.put("success", false);
	            response.put("message", "상태를 변경할 수 있는 생산 계획이 없거나, 이미 처리되었을 수 있습니다. (조건: " + mrpok + ")");

	            return ResponseEntity.status(HttpStatus.OK).body(response); //
	        }
	        
	        
	    } catch (IllegalArgumentException e) {
	        log.warn("작업 지시 생성 중 유효성 검사 오류: {}", e.getMessage());
	        response.put("success", false);
	        response.put("message", e.getMessage());
	        return ResponseEntity.badRequest().body(response);
	    } catch (Exception e) {
	        log.error("작업 지시 생성 중 서버 오류 발생: {}", e.getMessage(), e);
	        response.put("success", false);
	        response.put("message", "작업 지시 생성 중 서버 오류가 발생했습니다.");
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }
	}
	
	
	
	//품목검색 api
	@GetMapping("/api/item/search")
	@ResponseBody
	public ResponseEntity<List<ProductPlanDTO>> searchItem(@RequestParam(value = "query", required = false) String query){
		List<ProductPlanDTO> searchItem = service.searchItem(query);
		
		return ResponseEntity.ok(searchItem);
	}
	//생산계획 상태변경 status
	
	
	
	
	
	
	//생산계획 상세조회
	@GetMapping("/{planId}")
	@ResponseBody
	public ResponseEntity<ProductPlanDTO> productPlanDetail(@PathVariable("planId") String planId) {
		ProductPlanDTO detail = service.productPlanDetail(planId);
		
		return ResponseEntity.ok(detail); 
	}
	
	//생산계획 업데이트
	@PutMapping("/{planId}") // PUT 요청을 받고, URL에서 planId를 추출
	public ResponseEntity<Map<String, Object>> productPlanUpdate(
	        @PathVariable("planId") String planId, // <-- URL 경로의 planId를 받음
	        @Valid @RequestBody ProductPlanDTO dto, // <-- 요청 본문의 JSON 데이터를 DTO로 받음
	        BindingResult bindingResult) {

	    Map<String, Object> response = new HashMap<>();

	    if (bindingResult.hasErrors()) {
	        response.put("success", false);
	        response.put("message", "유효성 검사 실패: " + bindingResult.getAllErrors().get(0).getDefaultMessage());
	        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	    }

	    try {
	        // @PathVariable로 받은 planId를 DTO에 직접 설정해줄 수 있습니다.
	        // 클라이언트에서 JSON으로 보낸 DTO에 planId 필드가 포함되어 있을 수도 있지만,
	        // URL의 planId가 최종 식별자이므로 이 방식으로 명확하게 처리하는 것이 좋습니다.
	        dto.setPlanId(planId);

	        int result = service.productPlanUpdate(dto); // 서비스 호출

	        if (result > 0) {
	            response.put("success", true);
	            response.put("message", "생산 계획 수정이 완료되었습니다.");
	            return new ResponseEntity<>(response, HttpStatus.OK);
	        } else {
	            response.put("success", false);
	            response.put("message", "생산 계획 수정에 실패했습니다. (계획을 찾을 수 없거나 변경 사항 없음)");
	            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND); // 404 또는 적절한 상태 코드
	        }
	    } catch (Exception e) {
	        System.err.println("생산 계획 수정 중 오류 발생: " + e.getMessage());
	        response.put("success", false);
	        response.put("message", "서버 오류: " + e.getMessage());
	        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	    }
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