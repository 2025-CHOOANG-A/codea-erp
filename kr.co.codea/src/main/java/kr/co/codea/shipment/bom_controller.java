package kr.co.codea.shipment;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


//BOM
@Controller
@RequestMapping("/bom")
public class bom_controller {
	
   @GetMapping("/bom_detail")
   public String bom_detail() {
   return "bom/bom_detail"; 
 }
    	
  @GetMapping("/bom_list")
  public String bom_list() {
  return "bom/bom_list"; 
}

  @GetMapping("/bom_modify")
  public String bom_modify() {
  return "bom/bom_modify"; 
}

  @GetMapping("/bom_write")
  public String bom_write() {
  return "bom/bom_write"; 
    
     }

}
