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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class StorageController {
	private final StorageService service;
	
	public StorageController(StorageService service) {
		this.service = service;
	}
	
    @PostMapping("/storage/storage_register")
    @ResponseBody // 이 메서드만 JSON 응답을 반환하도록 지정
    public ResponseEntity<Map<String, Object>> registerStorage(@RequestBody StorageDTO dto) {
        Map<String, Object> response = new HashMap<>();

        try {
            int result = service.insertStorageList(dto);

            if (result > 0) {
                response.put("success", true);
                response.put("message", "창고 정보가 성공적으로 등록되었습니다.");
                return ResponseEntity.ok(response); // HTTP 200 OK
            } else {
                response.put("success", false);
                response.put("message", "창고 등록에 실패했습니다. 데이터베이스 문제일 수 있습니다.");
                return ResponseEntity.status(500).body(response); // HTTP 500 Internal Server Error 또는 400 Bad Request
            }
        } catch (Exception e) {
            // 예외 발생 시 처리
            response.put("success", false);
            response.put("message", "서버 처리 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response); // HTTP 500 Internal Server Error
        }
    }
	
	
	//창고게시판 등록 get링크
	@GetMapping("/storage/write")
	public String storage_list(Model m) {
        m.addAttribute("templateName", "storage/storage_write");
        m.addAttribute("fragmentName", "contentFragment");
		
		return "storage/storage_default";
	}
	
	//창고게시판 리스트 출력
	@GetMapping("/storage")
	public String storage_list(Model m,StorageDTO dto) {
		List<StorageDTO> list = service.storageListByKeyword(dto);
		
        m.addAttribute("storage", list);
        m.addAttribute("searchDto", dto); 
        
        m.addAttribute("templateName", "storage/storage_list");
        m.addAttribute("fragmentName", "contentFragment");
		
        return "storage/storage_default";
	}
	//창고게시판 상세 출력
	@GetMapping("/api/storage/{whId}")
	@ResponseBody
	public ResponseEntity<StorageDTO> storageDetail (@PathVariable("whId") Integer whId) {
		System.out.println("whId :" + whId);
    StorageDTO detail = service.getStorageDetailById(whId); 
    
    if (detail == null) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
		
	return new ResponseEntity<>(detail, HttpStatus.OK);	
	}
}
