package kr.co.codea.productplan;

import java.util.ArrayList;
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

import com.github.pagehelper.PageInfo;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/productplan")
public class ProductPlanController {
    private static final Logger log = LoggerFactory.getLogger(ProductPlanController.class);

    private ProductPlanService service;
    
    public ProductPlanController(ProductPlanService service) {
        this.service = service;
    }
    
    
    
    
    
    //작업지시 생성 API (자재 가출고 처리 포함)
    @PostMapping("/api/issue-work-orders")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> issueWorkOrders(@RequestBody ProductPlanDTO dto) {
        if (dto.getPlanIds() == null || dto.getPlanIds().isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "선택된 생산계획이 없습니다.");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            // 자재 가출고 처리 포함한 작업지시 생성
            Map<String, Object> result = service.changePlansStatus(dto.getPlanIds(), "작업지시", "작업지시");
            
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.status(HttpStatus.OK).body(result);
            }
            
        } catch (IllegalArgumentException e) {
            log.warn("작업지시 생성 중 유효성 검사 오류: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("작업지시 생성 중 서버 오류 발생: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "작업지시 생성 중 서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    

    //작업지시 취소 API (자재 할당 해제 포함) - 수정됨
    @PostMapping("/api/cancel-work-orders")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cancelWorkOrders(@RequestBody ProductPlanDTO dto) {
        if (dto.getPlanIds() == null || dto.getPlanIds().isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "선택된 생산계획이 없습니다.");
            return ResponseEntity.badRequest().body(errorResponse); // 수정: ResponseEntity.badRequest() 추가
        }

        try {
            Map<String, Object> result = service.cancelWorkOrders(dto.getPlanIds());
            
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.status(HttpStatus.OK).body(result);
            }
            
        } catch (IllegalArgumentException e) {
            log.warn("작업지시 취소 중 유효성 검사 오류: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("작업지시 취소 중 서버 오류 발생: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "작업지시 취소 중 서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 여러 계획의 자재소요량 일괄 확인 API
     */
    @PostMapping("/api/check-material-requirements")
    @ResponseBody 
    public Map<String, Object> checkMaterialRequirements(@RequestBody Map<String, List<String>> request) {
        List<String> planIds = request.get("planIds");
        
        // 1. planIds가 제대로 전달되는지 로깅
        log.info("받은 planIds: {}", planIds);
        
        if (planIds == null || planIds.isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "선택된 생산계획이 없습니다.");
            return errorResponse;
        }
        
        return service.checkMaterialAvailability(planIds);
    }
    
    //자재소요량 확인 API
    @GetMapping("/api/material-requirements/{planId}")
    @ResponseBody
    public Map<String, Object> getMaterialRequirements(@PathVariable("planId") String planId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<MaterialRequirementDTO> materials = service.getMaterialRequirements(planId);
            
            if (materials == null || materials.isEmpty()) {
                response.put("success", false);
                response.put("message", "자재 소요량 데이터가 없습니다.");
                return response;
            }
            
            // 재고 부족 자재 확인
            List<Map<String, Object>> materialStatus = new ArrayList<>();
            boolean hasInsufficientMaterials = false;
            
            for (MaterialRequirementDTO material : materials) {
                int availableQty = service.getAvailableInventory(material.getMaterialId());
                
                Map<String, Object> materialInfo = new HashMap<>();
                materialInfo.put("itemCode", material.getItemCode());
                materialInfo.put("itemName", material.getItemName());
                materialInfo.put("requiredQty", material.getRequiredQty());
                materialInfo.put("availableQty", availableQty);
                materialInfo.put("unit", material.getUnit());
                materialInfo.put("shortage", Math.max(0, material.getRequiredQty() - availableQty));
                materialInfo.put("sufficient", availableQty >= material.getRequiredQty());
                
                if (availableQty < material.getRequiredQty()) {
                    hasInsufficientMaterials = true;
                }
                
                materialStatus.add(materialInfo);
            }
            
            response.put("success", true);
            response.put("materials", materialStatus);
            response.put("hasInsufficientMaterials", hasInsufficientMaterials);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "자재 소요량 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 품목검색 API
     */
    @GetMapping("/api/item/search")
    @ResponseBody
    public ResponseEntity<List<ProductPlanDTO>> searchItem(@RequestParam(value = "query", required = false) String query){
        List<ProductPlanDTO> searchItem = service.searchItem(query);
        return ResponseEntity.ok(searchItem);
    }
    
    /**
     * 생산계획 상세조회
     */
    @GetMapping("/{planId}")
    @ResponseBody
    public ResponseEntity<ProductPlanDTO> productPlanDetail(@PathVariable("planId") String planId) {
        ProductPlanDTO detail = service.productPlanDetail(planId);
        return ResponseEntity.ok(detail); 
    }
    
    /**
     * 생산계획 업데이트
     */
    @PutMapping("/{planId}")
    public ResponseEntity<Map<String, Object>> productPlanUpdate(
            @PathVariable("planId") String planId,
            @Valid @RequestBody ProductPlanDTO dto,
            BindingResult bindingResult) {

        Map<String, Object> response = new HashMap<>();

        if (bindingResult.hasErrors()) {
            response.put("success", false);
            response.put("message", "유효성 검사 실패: " + bindingResult.getAllErrors().get(0).getDefaultMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        try {
            dto.setPlanId(planId);
            int result = service.productPlanUpdate(dto);

            if (result > 0) {
                response.put("success", true);
                response.put("message", "생산 계획 수정이 완료되었습니다.");
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.put("success", false);
                response.put("message", "생산 계획 수정에 실패했습니다. (계획을 찾을 수 없거나 변경 사항 없음)");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            log.error("생산 계획 수정 중 오류 발생: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "서버 오류: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 생산계획 페이징 엔드포인트
     */
    @GetMapping("/page")
    public String page(Model m, 
                      ProductPlanDTO dto,
                      @RequestParam(defaultValue = "1") int page,
                      @RequestParam(defaultValue = "10") int size) {
        PageInfo<ProductPlanDTO> pageInfo = service.getpages(dto, page, size);
        
        m.addAttribute("productionPlans", pageInfo.getList());
        m.addAttribute("pageInfo", pageInfo);
        m.addAttribute("searchDto", dto); 
        m.addAttribute("templateName", "productplan/productplan_list");
        m.addAttribute("fragmentName", "contentFragment");
        
        return "productplan/productplan_default";
    }
    
    /**
     * 생산계획 리스트
     */
    @GetMapping
    public String productPlanList(Model m, ProductPlanDTO dto,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        
        PageInfo<ProductPlanDTO> pageInfo = service.getpages(dto, page, size);
        
        m.addAttribute("productionPlans", pageInfo.getList());
        m.addAttribute("pageInfo", pageInfo);
        m.addAttribute("searchDto", dto); 
        m.addAttribute("templateName", "productplan/productplan_list");
        m.addAttribute("fragmentName", "contentFragment");
        
        return "productplan/productplan_default";
    }
    
    /**
     * 생산계획 생성
     */
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
                response.put("message", "생산 계획 등록에 실패했습니다.");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("생산 계획 등록 중 오류 발생: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "서버 에러");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}