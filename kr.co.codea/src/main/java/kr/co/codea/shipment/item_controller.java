package kr.co.codea.shipment;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


//제품 
@Controller
@RequestMapping("/item")
public class item_controller {
	
	 @GetMapping("/item_detail")
	 public String item_detail() {
	      return "item/item_detail"; 
	 }
	    	
	@GetMapping("/item_list")
	public String item_list() {
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
