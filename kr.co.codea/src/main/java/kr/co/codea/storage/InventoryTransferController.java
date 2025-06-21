package kr.co.codea.storage;

import kr.co.codea.auth.dto.UserDetailsDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

@Controller
@RequestMapping("/inventory-transfer")
public class InventoryTransferController {
    private static final Logger log = LoggerFactory.getLogger(InventoryTransferController.class);
    
    private final InventoryTransferService service;
    
    public InventoryTransferController(InventoryTransferService service) {
        this.service = service;
    }
    

    /**
     * Spring Security 컨텍스트에서 현재 로그인한 사용자의 empId를 가져옵니다.
     * @return 로그인한 사용자의 empId, 없으면 null
     */
    private Integer getCurrentUserEmpId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetailsDto) {
            UserDetailsDto userDetails = (UserDetailsDto) auth.getPrincipal();
            // UserDetailsDto의 getEmpId()가 Long을 반환한다고 가정하고 Integer로 변환
            return userDetails.getEmpId() != null ? userDetails.getEmpId().intValue() : null;
        }
        return null; // 인증 정보가 없으면 null 반환
    }
    
    // 재고 이동 목록 페이지
    @GetMapping
    public String transferList(Model m, @ModelAttribute InventoryTransferDTO dto) {
        // 페이징 기본값 설정
        if (dto.getPage() == null || dto.getPage() < 1) {
            dto.setPage(1);
        }
        if (dto.getSize() == null || dto.getSize() < 1) {
            dto.setSize(10);
        }
        
        try {
            // 총 개수 조회
            int totalCount = service.getTransferCount(dto);
            dto.setTotalCount(totalCount);
            
            // 목록 조회
            List<InventoryTransferDTO> list = service.getTransferList(dto);
            
            m.addAttribute("transfers", list);
            m.addAttribute("searchDto", dto);
            m.addAttribute("currentPage", dto.getPage());
            m.addAttribute("totalPages", dto.getTotalPages());
            m.addAttribute("totalCount", totalCount);
            m.addAttribute("pageSize", dto.getSize());
        } catch (Exception e) {
            log.error("재고 이동 목록 조회 실패", e);
            m.addAttribute("transfers", new ArrayList<>());
            m.addAttribute("error", "목록 조회 중 오류가 발생했습니다.");
        }
        
        return "inventory/transfer_list";
    }
    
    // 창고별 품목 목록 API
    @GetMapping("/api/warehouse/{whId}/items")
    @ResponseBody
    public ResponseEntity<List<InventoryTransferDTO>> getWarehouseItems(
            @PathVariable(name = "whId") Integer whId,
            @RequestParam(name = "keyword", required = false) String keyword) {
        
        try {
            List<InventoryTransferDTO> items = service.getWarehouseItems(whId, keyword);
            return ResponseEntity.ok(items != null ? items : new ArrayList<>());
        } catch (Exception e) {
            log.error("창고별 품목 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }
    
    // 재고 이동 유효성 검사 API
    @PostMapping("/api/validate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> validateTransfer(@RequestBody InventoryTransferDTO dto) {
        try {
            Map<String, Object> result = service.validateTransfer(
                dto.getItemId(), dto.getFromWhId(), dto.getToWhId(), dto.getQuantity());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("재고 이동 유효성 검사 중 오류", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "유효성 검사 중 서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
        }
    }
    
    /**
     * 재고 이동 실행(요청) API
     * 로그인한 사용자의 ID를 자동으로 empId로 설정합니다.
     */
    @PostMapping("/api/execute")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> executeTransfer(@RequestBody InventoryTransferDTO dto) {
        Map<String, Object> response = new HashMap<>();
        try {
            Integer currentEmpId = getCurrentUserEmpId();
            if (currentEmpId == null) {
                response.put("success", false);
                response.put("message", "로그인 정보가 유효하지 않습니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            dto.setEmpId(currentEmpId); // 로그인한 사용자 ID 설정
            Map<String, Object> result = service.createTransfer(dto);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("재고 이동 실행 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "재고 이동 요청 중 서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 재고 이동 취소 API
     * 로그인한 사용자의 ID를 사용하여 취소 요청을 처리합니다.
     */
    @PostMapping("/api/{transferId}/cancel")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cancelTransfer(@PathVariable("transferId") Integer transferId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Integer currentEmpId = getCurrentUserEmpId();
            if (currentEmpId == null) {
                response.put("success", false);
                response.put("message", "로그인 정보가 유효하지 않습니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            Map<String, Object> result = service.cancelTransfer(transferId, currentEmpId);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("재고 이동 취소 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "재고 이동 취소 요청 중 서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
