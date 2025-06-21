// StorageController.java - 수정 페이지 분리하여 오류 해결
package kr.co.codea.storage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/storage") 
public class StorageController {
    private final StorageService service;

    public StorageController(StorageService service) {
        this.service = service;
    }

    // 창고 목록 페이지
    @GetMapping 
    public String storageList(Model m, @ModelAttribute StorageDTO dto,
                             @RequestParam(value = "withInventory", defaultValue = "true") boolean withInventory) {
        try {
            // 페이징 기본값 설정
            if (dto.getPage() == null || dto.getPage() < 1) {
                dto.setPage(1);
            }
            if (dto.getSize() == null || dto.getSize() < 1) {
                dto.setSize(10);
            }
            
            // 총 개수 조회
            int totalCount = service.getStorageCount(dto);
            dto.setTotalCount(totalCount);
            dto.calculateTotalPages();
            
            // 목록 조회
            List<StorageDTO> list;
            if (withInventory) {
                list = service.storageListWithInventory(dto);
            } else {
                list = service.storageListByKeyword(dto);
            }

            m.addAttribute("storage", list);
            m.addAttribute("searchDto", dto);
            m.addAttribute("currentPage", dto.getPage());
            m.addAttribute("totalPages", dto.getTotalPages());
            m.addAttribute("totalCount", totalCount);
            m.addAttribute("pageSize", dto.getSize());
            m.addAttribute("withInventory", withInventory);

            m.addAttribute("templateName", "storage/storage_list");
            m.addAttribute("fragmentName", "contentFragment");

            return "storage/storage_default";
        } catch (Exception e) {
            m.addAttribute("error", "목록 조회 중 오류가 발생했습니다: " + e.getMessage());
            m.addAttribute("storage", List.of());
            m.addAttribute("templateName", "storage/storage_list");
            m.addAttribute("fragmentName", "contentFragment");
            return "storage/storage_default";
        }
    }

    // 창고 등록 페이지
    @GetMapping("/write")
    public String storageRegisterForm(Model m) {
        m.addAttribute("templateName", "storage/storage_write");
        m.addAttribute("fragmentName", "contentFragment");
        m.addAttribute("formTitle", "창고 정보 등록");
        m.addAttribute("submitButtonText", "등록");
        return "storage/storage_default";
    }

    // 창고 수정 페이지 (분리됨 - 오류 해결)
    @GetMapping("/{id}/edit")
    public String editStorageForm(@PathVariable("id") Integer id, Model m) { 
        try {
            StorageDTO storage = service.getStorageDetailById(id);

            if (storage == null) {
                return "redirect:/storage?error=" + "창고 정보를 찾을 수 없습니다."; 
            }
            
            m.addAttribute("storage", storage);
            m.addAttribute("templateName", "storage/storage_edit"); // 별도 템플릿
            m.addAttribute("fragmentName", "contentFragment");

            return "storage/storage_default";
        } catch (Exception e) {
            return "redirect:/storage?error=" + "페이지 로딩 중 오류가 발생했습니다.";
        }
    }

    // === API 엔드포인트들 ===
    
    // 창고 상세 정보 API
    @GetMapping("/api/{id}") 
    @ResponseBody
    public ResponseEntity<StorageDTO> storageDetail(@PathVariable("id") Integer id) {
        try {
            StorageDTO detail = service.getStorageDetailById(id);
            if (detail == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(detail, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 창고 정보 수정 API
    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateStorage(@PathVariable("id") Integer id, @RequestBody StorageDTO dto) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (!id.equals(dto.getWhId())) {
                response.put("success", false);
                response.put("message", "요청 ID와 데이터 ID가 일치하지 않습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            int updatedRows = service.updateStorage(dto);
            if (updatedRows > 0) {
                response.put("success", true);
                response.put("message", "창고 정보가 성공적으로 업데이트되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "창고를 찾을 수 없거나, 변경된 내용이 없습니다.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "수정 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 창고 정보 등록 API
    @PostMapping 
    @ResponseBody
    public ResponseEntity<Map<String, Object>> registerStorage(@RequestBody StorageDTO dto) {
        Map<String, Object> response = new HashMap<>();

        try {
            int result = service.insertStorageList(dto);

            if (result > 0) {
                response.put("success", true);
                response.put("message", "창고 정보가 성공적으로 등록되었습니다.");
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                response.put("success", false);
                response.put("message", "창고 등록에 실패했습니다.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "서버 처리 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 사원 검색 API
    @GetMapping("/api/employees/search") 
    @ResponseBody
    public ResponseEntity<List<StorageDTO>> searchEmployees(@RequestParam(value = "query", required = false) String query) {
        try {
            // null 또는 빈 문자열 체크
            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.ok(List.of()); // 빈 리스트 반환
            }
            
            List<StorageDTO> searchResult = service.searchEmpNo(query.trim());
            
            if (searchResult == null) {
                return ResponseEntity.ok(List.of());
            }
            
            return ResponseEntity.ok(searchResult);
            
        } catch (Exception e) {
            // 로그 출력
            System.err.println("직원 검색 API 오류: " + e.getMessage());
            e.printStackTrace();
            
            // 빈 리스트 반환 (500 에러 대신)
            return ResponseEntity.ok(List.of());
        }
    }
    
}