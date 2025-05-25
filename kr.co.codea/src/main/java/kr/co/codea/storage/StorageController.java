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

    // 창고 정보 수정
    @PutMapping("/{id}") //
    public ResponseEntity<?> updateStorage(@PathVariable("id") Integer id, @RequestBody StorageDTO dto) {
        if (!id.equals(dto.getWhId())) {
            return new ResponseEntity<>(Map.of("message", "요청 ID와 데이터 ID가 일치하지 않습니다."), HttpStatus.BAD_REQUEST);
        }
        int updatedRows = service.updateStorage(dto);
        if (updatedRows > 0) {
            return new ResponseEntity<>(Map.of("message", "창고 정보가 성공적으로 업데이트되었습니다."), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(Map.of("message", "창고를 찾을 수 없거나, 변경된 내용이 없습니다."), HttpStatus.NOT_FOUND);
        }
    }

    // 창고 수정 페이지 (GET)
    @GetMapping("/{id}/edit") // /storage/{id}/edit
    public String editStorageForm(@PathVariable("id") Integer id, Model m) { 
        StorageDTO storage = service.getStorageDetailById(id);

        if (storage == null) {
            return "redirect:/storage"; // 존재하지 않는 창고는 목록으로 리다이렉트
        }
        m.addAttribute("storage", storage);
        m.addAttribute("templateName", "storage/storage_write"); 
        m.addAttribute("fragmentName", "contentFragment");
        m.addAttribute("formTitle", "창고 정보 수정"); //  추가: 폼 제목 동적으로 설정
        m.addAttribute("submitButtonText", "수정"); //  버튼 텍스트 동적으로 설정

        return "storage/storage_default";
    }

    // 사원 검색 API (API)
    @GetMapping("/api/employees/search") 
    @ResponseBody
    public ResponseEntity<List<StorageDTO>> searchEmployees(@RequestParam(value = "query", required = false) String query) {
        List<StorageDTO> searchEmpNo = service.searchEmpNo(query);
        return ResponseEntity.ok(searchEmpNo);
    }

    // 창고 정보 등록
    @PostMapping 
    public ResponseEntity<Map<String, Object>> registerStorage(@RequestBody StorageDTO dto) {
        Map<String, Object> response = new HashMap<>();

        try {
            int result = service.insertStorageList(dto);

            if (result > 0) {
                response.put("success", true);
                response.put("message", "창고 정보가 성공적으로 등록되었습니다.");
                return ResponseEntity.status(HttpStatus.CREATED).body(response); // ✨ 변경: HTTP 201 Created
            } else {
                response.put("success", false);
                response.put("message", "창고 등록에 실패했습니다. 데이터베이스 문제일 수 있습니다.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "서버 처리 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 창고 등록 페이지
    @GetMapping("/write")
    public String storageRegisterForm(Model m) { // 메서드 이름 변경
        m.addAttribute("templateName", "storage/storage_write"); // 등록/수정 공용 템플릿 사용
        m.addAttribute("fragmentName", "contentFragment");
        m.addAttribute("formTitle", "창고 정보 등록"); // 폼 제목 동적으로 설정
        m.addAttribute("submitButtonText", "등록"); // 버튼 텍스트 동적으로 설정

        return "storage/storage_default";
    }

    // 창고 목록 페이지 
    @GetMapping 
    public String storageList(Model m, @ModelAttribute StorageDTO dto) { // @ModelAttribute 사용
        List<StorageDTO> list = service.storageListByKeyword(dto);

        m.addAttribute("storage", list);
        m.addAttribute("searchDto", dto);

        m.addAttribute("templateName", "storage/storage_list");
        m.addAttribute("fragmentName", "contentFragment");

        return "storage/storage_default";
    }

    //창고 상세 정보 API
    @GetMapping("/api/{id}") 
    @ResponseBody
    public ResponseEntity<StorageDTO> storageDetail(@PathVariable("id") Integer id) { // whId -> id
        System.out.println("id :" + id);
        StorageDTO detail = service.getStorageDetailById(id);

        if (detail == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(detail, HttpStatus.OK);
    }
}