package kr.co.codea.item;

import java.util.List;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.annotation.Resource;
import kr.co.codea.order.OrderController;
import kr.co.codea.productplan.ProductPlanController;
import lombok.RequiredArgsConstructor;


//제품 
@Controller
@RequestMapping("/item")
public class item_controller {

 
	Logger log = LoggerFactory.getLogger(this.getClass());
	
	
	@Autowired
	itemDAO dao;
	
	
	 @GetMapping("/list")
	    public String listPage() {
	        return "item/item_list"; 
	    }
	    
	
	 @GetMapping("/item_detail")
	 public String item_detail(@RequestParam("itemId") Integer itemId, Model m) {		   
		 List<itemDTO> item_detail_list = this.dao.item_detail();
	     m.addAttribute("item_detail_list", item_detail_list);
	     System.out.println(item_detail_list);
	 
	     return "item/item_detail";
	 }	
	 //주문목록 , 검색창 핸들링 
	 @GetMapping("/item_list")
	 public String itemList(Model m,
			 @RequestParam(name = "keyword", required = false) String keyword) {
		    List<itemDTO> item_list = this.dao.item_select();
		    m.addAttribute("item_list", item_list);//주문목록 리스트 
		   System.out.println(item_list);
		  // m.addAttribute("keyword", keyword); // 검색어 유지용
		    return "item/item_list";
		}
	 
	 //주문수정
	@GetMapping("/item_modify")
	public String item_modify() {
	    return "item/item_modify"; 
	}
	
	
    //제품 등록 화면 이동
	@GetMapping("/item_write")
	public String item_write(Model m) {
		List<itemDTO> bp_list = this.dao.bp_select();
		List<itemDTO> calL = this.dao.selectCatL();
		List<itemDTO> calS = this.dao.selectCatS();
		List<itemDTO> uni_code = this.dao.unitcode_list();
		m.addAttribute("bp_list", bp_list); //거래처 정보 선택
		m.addAttribute("calL",calL); //대분류 항목만 가져옴
		m.addAttribute("calS", calS); //소분류 항목만 가져옴
		m.addAttribute("uni_code", uni_code); //단위 항목만 가져옴
		//m.addAttribute("item_cg_list", item_cg_list); //대분류 소분류 항목 조회 선택
		//System.out.println(uni_code);
		//System.out.println(calL);
		//System.out.println(calS);
	    return "item/item_write"; 
	}	
	
	@PostMapping("/item_writeok")
	public String item_wirteok(@ModelAttribute itemDTO dto, Model m) {
		this.dao.insert_item(dto);//제품등록(item)
		
		//저장 후 직접 리스트 다시 조회 
		List<itemDTO> item_list = this.dao.item_select();
		m.addAttribute("item_list", item_list);
				
	    return "item/item_list";
	}

}
