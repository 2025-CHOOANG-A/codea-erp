package kr.co.codea.item;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.annotation.Resource;


//제품 
@Controller
@RequestMapping("/item")
public class item_controller {
	
	Logger log = LoggerFactory.getLogger(this.getClass());
	
	
	@Resource(name="item_list_DTO")
	item_list_DTO item_lits_DTO;
	
	@Autowired
	private item_service service;
	
	
	 @GetMapping("/item_detail")
	 public String item_detail() {
	      return "item/item_detail"; 
	 }
	    	
	 //주문목록 , 검색창 핸들링 
	 @GetMapping("/item_list")
	 public String itemList(Model m,
			 @RequestParam(name = "keyword", required = false) String keyword) {
		

		
		    List<item_list_DTO> result = service.item_search(keyword);
		    m.addAttribute("itemlist", result);
		   m.addAttribute("keyword", keyword); // 검색어 유지용
		    return "item/item_list";
		}
	 
	@GetMapping("/item_modify")
	public String item_modify() {
	    return "item/item_modify"; 
	}

	@GetMapping("/item_write")
	public String item_write() {
		
		
		
		
	    return "item/item_write"; 
	}	

}
