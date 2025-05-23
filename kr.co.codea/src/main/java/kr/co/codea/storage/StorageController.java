package kr.co.codea.storage;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class StorageController {
	private final StorageService service;
	
	public StorageController(StorageService service) {
		this.service = service;
	}
	
	@PostMapping("/storage/storage_register")
	public String storage_list(@ModelAttribute(name = "dto") StorageDTO
			dto,RedirectAttributes ra) {
		
    	int result = service.insertStorageList(dto);
		
    	if (result > 0) {
            ra.addFlashAttribute("","");
            return "redirect:/storage"; 
        } else {
        	 ra.addFlashAttribute("error", "창고 등록에 실패했습니다. 다시 시도해주세요.");
            return "redirect:/storage/write";
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
