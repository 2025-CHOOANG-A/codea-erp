package kr.co.codea.storage;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class StorageController {
	private final StorageService service;
	
	public StorageController(StorageService service) {
		this.service = service;
	}
	
	@GetMapping("/storage")
	public String storage_list(Model m,StorageDTO dto) {
		List<StorageDTO> list = service.storageListByKeyword(dto);
		
        m.addAttribute("storage", list);
        m.addAttribute("searchDto", dto); 
        
        m.addAttribute("templateName", "storage/storage_list");
        m.addAttribute("fragmentName", "contentFragment");
		
        return "storage/storage_default";
	}
	@GetMapping("/api/storage/{whId}")
	@ResponseBody
	public StorageDTO storageDetail (@PathVariable("whID") Integer whId) {
    StorageDTO detail = service.getStorageDetailById(whId); 

		
	return null;	
	}
}
