package kr.co.codea.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
		  
	
	@Autowired
	itemDTO dto;
	
	 @GetMapping("/item_detail")
	 public String item_detail(@RequestParam("itemId") String itemId, Model m) {		   
		 
		 /*제품정보*/
		 itemDTO item = this.dao.select_item_by_id(itemId);
		 m.addAttribute("item", item); 
	
		 /*거래처 정보*/
	
		  List<itemDTO> item_detail_list = this.dao.item_detail();
	     m.addAttribute("item_detail_list", item_detail_list);
	    // m.addAttribute("itemId", itemId);
	    
	     //System.out.println(item_detail_list);
	     //System.out.println(itemId);
	 
	     return "item/item_detail";
	 }	
	 
	 
	 @GetMapping("/item_list")
	    public String itemList(
	            Model m,
	            @RequestParam(name = "keyword", required = false) String keyword,
	            @RequestParam(name = "itemType", required = false) String itemType,
	            @RequestParam(name = "page", defaultValue = "1") int page
	    ) {
	        int pageSize = 10; // 페이지당 보여줄 행 수
	        int offset = (page - 1) * pageSize;

	        Map<String, Object> params = new HashMap();
	        params.put("keyword", (keyword == null || keyword.isEmpty()) ? null : keyword);
	        params.put("itemType", (itemType == null || itemType.isEmpty()) ? null : itemType);
	        params.put("offset", offset);
	        params.put("pageSize", pageSize);

	        // 1) 페이징된 리스트 조회
	        List<itemDTO> pagedList = this.dao.select_item_page(params);
	        // 2) 전체 건수 조회
	        int totalCount = this.dao.page_count(params);
	        int totalPages = (int) Math.ceil((double) totalCount / pageSize);

	        m.addAttribute("item_list", pagedList);
	        m.addAttribute("keyword", keyword);
	        m.addAttribute("itemType", itemType);
	        m.addAttribute("currentPage", page);
	        m.addAttribute("totalPages", totalPages);
	        m.addAttribute("itemTypes", List.of("완제품","원자재")); // 예시
	        m.addAttribute("totalCount", totalCount);

	        System.out.println(pagedList);
	        return "item/item_list"; // Thymeleaf 템플릿 이름
	    }
	
	 
	 
	 
	 //주문수정
	 /*
	@GetMapping("/item_modify")
	public String item_modify() {
	    return "item/item_modify"; 
	}
	*/
	 
	  @GetMapping("/item_edite")
	    public String item_modify(@RequestParam("itemId") String itemId, Model m) {
		   itemDTO item = this.dao.select_item_by_id(itemId);
	        m.addAttribute("item", item);
	        
	        
	        List<itemDTO> bp_list = this.dao.bp_select();
	    	List<itemDTO> calL = this.dao.selectCatL();
			List<itemDTO> calS = this.dao.selectCatS();
			List<itemDTO> uni_code = this.dao.unitcode_list();
			m.addAttribute("bp_list", bp_list); //거래처 정보 선택
			m.addAttribute("calL",calL); //대분류 항목만 가져옴
			m.addAttribute("calS", calS); //소분류 항목만 가져옴
			m.addAttribute("uni_code", uni_code); //단위 항목만 가져옴
	        
	        System.out.println(item);
	        return "item/item_edite"; // 수정 폼 페이지
	    }

	    /**
	     * 제품 수정 처리
	     */
	  
	  
	  @PostMapping("/item_editeok")
	    public String item_modifyok(@ModelAttribute itemDTO dto, Model model) {
	        int result = dao.update_item(dto); // 수정 실행

	        if (result > 0) {
	            // 1) 수정이 성공했으면 → 목록으로 리다이렉트
	            return "redirect:/item/item_list";
	        } else {
	            // 2) 수정이 실패했으면 → 다시 수정 폼으로 돌아갈 때, 
	            //    dto 객체를 모델에 담아서 폼이 기존 값을 유지하도록
	            model.addAttribute("error", "수정 실패");
	            model.addAttribute("item", dto);
	            return "item/item_edit"; 
	        }
	    }
/*
	    @PostMapping("/item_editeok")
	    public String item_modifyok(@ModelAttribute itemDTO dto, Model model) {
	        int result = dao.update_item(dto); // 수정 실행

	        if (result > 0) {
	            List<itemDTO> item_list = dao.item_select(); // 전체 목록 다시 조회
	            model.addAttribute("item_list", item_list);
	            return "item/item_list"; // 목록 페이지로 이동 (redirect 없이)
	        } else {
	            model.addAttribute("error", "수정 실패");
	            model.addAttribute("item", dto);
	            return "item/item_list"; // 수정폼으로 복귀
	        }
	    }
	*/
	
	  
	  
	//제품 삭제
	  
	  
	  @PostMapping("/item_delete")
	  public String item_delete(@RequestParam("itemId") String itemId, RedirectAttributes rttr) {
	      int result = dao.delete_item(itemId);
	      if (result > 0) {
	          // 삭제 성공 → 목록으로 리다이렉트
	          rttr.addFlashAttribute("msg", "삭제되었습니다.");
	          return "redirect:/item/item_list";
	      } else {
	          // 삭제 실패 → 목록으로 리다이렉트하되, 실패 메시지를 쿼리스트링이나 FlashAttribute로 넘길 수 있음
	          rttr.addFlashAttribute("error", "삭제에 실패했습니다.");
	          return "redirect:/item/item_list";
	      }
	  }
	  /*
	    @PostMapping("/item_delete")
	    public String item_delete(@RequestParam("itemId") String itemId, Model model) {
	        int result = dao.delete_item(itemId);
	        if (result > 0) {
	            // 삭제 성공 → 목록으로 리다이렉트
	            return "redirect:/item/item_list";
	        } else {
	            // 삭제 실패 시 에러 메시지를 보여주거나 목록으로 돌아가기
	            model.addAttribute("error", "삭제에 실패했습니다.");
	            // 다시 목록을 보여주도록 처리하거나, 상세 페이지로 복귀할 수 있음
	            return "item/item_list";
	        }
	    }
	  */
	  
	
	
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
