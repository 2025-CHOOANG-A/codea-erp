package kr.co.codea.bom;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


//BOM
@Controller
@RequestMapping("/bom")
public class bom_controller {
	
	 
	Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	bomDAO b_dao;
	
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
  public String bom_write(Model m) {
	  
	  //완제품 리스트 조회
	  List<bomDTO> bom_item_y_list= this.b_dao.bom_item_type_y();
	  m.addAttribute("bom_item_y_list", bom_item_y_list);
	  //System.out.println(bom_item_y_list);
	  
  return "bom/bom_write";   
     }
    
}
