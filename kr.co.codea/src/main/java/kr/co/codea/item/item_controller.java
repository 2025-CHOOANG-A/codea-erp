package kr.co.codea.item;

import java.util.ArrayList;
import java.util.List;
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
		 
		 itemDTO item = this.dao.select_item_by_id(itemId);
		 m.addAttribute("item", item); 
		// System.out.println(item);
		  
		  List<itemDTO> item_detail_list = this.dao.item_detail();
	     m.addAttribute("item_detail_list", item_detail_list);
	     m.addAttribute("itemId", itemId);
	     //System.out.println(item_detail_list);
	 
	     return "item/item_detail";
	 }	
	 
	 
	 @GetMapping("/item_list")
	 public String itemList(Model m,
	                        @RequestParam(name = "keyword", required = false) String keyword,
	                        @RequestParam(name = "itemType", required = false) String itemType,
	                        @RequestParam(name = "page", defaultValue = "1") int page) {

	     // 1. 전체 리스트 가져오기
	     List<itemDTO> fullList = this.dao.item_select();

	     // 2. 검색 필터 처리
	     List<itemDTO> filteredList = fullList.stream()
	         .filter(i ->
	             (keyword == null || keyword.isEmpty() || i.getItemName().contains(keyword) || i.getItemCode().contains(keyword)) &&
	             (itemType == null || itemType.isEmpty() || i.getItemType().equals(itemType))
	         )
	         .collect(Collectors.toList());

	     // 3. 페이징 처리
	     int pageSize = 10;
	     int totalItems = filteredList.size();
	     int totalPages = (int) Math.ceil((double) totalItems / pageSize);
	     int start = (page - 1) * pageSize;
	     int end = Math.min(start + pageSize, totalItems);
	     List<itemDTO> pagedList = start >= totalItems ? new ArrayList<>() : filteredList.subList(start, end);

	     // 4. 뷰에 전달
	     m.addAttribute("item_list", pagedList);
	     m.addAttribute("keyword", keyword);
	     m.addAttribute("itemType", itemType);
	     m.addAttribute("currentPage", page);
	     m.addAttribute("totalPages", totalPages);
	  

	     return "item/item_list";
	 }
	 
		 //완제품, 원제품 구분 
		 /*
		    List<itemDTO> item_list = this.dao.item_select();
		    m.addAttribute("item_list", item_list);//주문목록 리스트 
		    m.addAttribute("keyword", keyword);
		    System.out.println(keyword);
		    //System.out.println(item_list);
		  // m.addAttribute("keyword", keyword); // 검색어 유지용
		    return "item/item_list";
		}

*/
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
	            List<itemDTO> item_list = dao.item_select(); // 전체 목록 다시 조회
	            model.addAttribute("item_list", item_list);
	            return "item/item_list"; // 목록 페이지로 이동 (redirect 없이)
	        } else {
	            model.addAttribute("error", "수정 실패");
	            model.addAttribute("item", dto);
	            return "item/item_list"; // 수정폼으로 복귀
	        }
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
