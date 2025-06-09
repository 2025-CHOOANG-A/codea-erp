package kr.co.codea.bom;

import java.util.HashMap;
import java.util.LinkedHashMap;
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

import kr.co.codea.item.itemDTO;


//BOM
@Controller
@RequestMapping("/bom")
public class bom_controller {
	
	 
	Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	bomDAO b_dao;
	
	
	
	@GetMapping("/bom_detail")
	public String bom_detail(@RequestParam("bomCode") String bomCode, Model m) {
         
		//detail, edit 전송
		  bomDTO bom= this.b_dao.select_bom_by_bom(bomCode);
		  m.addAttribute("bomCode",bomCode);
		
		
		  //bom 코드번호 기준으로 조회하게 하기 
		// m.addAttribute("selectedBomCode", bomCode);
		
		  // ⛳️ 이 부분 추가! 단일 BOM 헤더만 조회
		    bomDTO header = this.b_dao.selectBomHeaderByCode(bomCode);
		   m.addAttribute("header", header); 
	      // System.out.println(header);
		 
        // 자재 , 원자재로 넘기기 		
		List<bomDTO> select_header_list = this.b_dao.bom_item_type_y(); //완제품 
		m.addAttribute("select_header_list", select_header_list);
		
		List<bomDTO> select_detail_list = this.b_dao.bom_item_type_j(); //원자재		
		m.addAttribute("select_detail_list", select_detail_list);
		
		System.out.println(select_header_list);
		//System.out.println(select_detail_list);
	    return "bom/bom_detail"; 
	}
		
	
  //BOM 목록
	

    /**
     *  /bom_list 요청 시 
     *  • keyword: 검색어 (BOM 코드, 제품 코드, 제품명 중 하나를 LIKE 조회)
     *  • page   : 현재 페이지 번호 (기본값 1)
     *  • size   : 한 페이지당 보여줄 건수 (기본값 10)
     *  • sortField: 정렬 대상 컬럼명 (예: "H.BOM_HEADER_ID", "I.ITEM_NAME" 등)
     *  • sortOrder: 정렬 방향 ("ASC" 또는 "DESC")
     */
	
	@GetMapping("/bom_list")
	public String bom_list(
			
	        @RequestParam(value = "keyword",   required = false, defaultValue = "")   String keyword,
	        @RequestParam(value = "page",      required = false, defaultValue = "1")  String pageStr,
	        @RequestParam(value = "size",      required = false, defaultValue = "3") String sizeStr,
	        @RequestParam(value = "sortField", required = false, defaultValue = "bomCode") String sortField,
	        @RequestParam(value = "sortOrder", required = false, defaultValue = "ASC") String sortOrder,
	        Model m
	) {
	    // 1) pageStr, sizeStr → int 변환 및 기본값 처리
	    int page;
	    int size;
	    try {
	        page = Integer.parseInt(pageStr);
	        if (page < 1) page = 1;
	    } catch (NumberFormatException e) {
	        page = 1;
	    }
	    try {
	        size = Integer.parseInt(sizeStr);
	        if (size < 1) size = 3;
	    } catch (NumberFormatException e) {
	        size = 10;
	    }

	    // 2) offset 계산
	    int offset = (page - 1) * size;

	    // 3) DAO에 넘길 파라미터 맵 준비
	    Map<String, Object> params = new HashMap<>();
	    params.put("keyword",   keyword);    // 검색어 (빈 문자열 허용)
	    params.put("sortField", sortField);  // ex) "H.BOM_HEADER_ID" or "I.ITEM_NAME"
	    params.put("sortOrder", sortOrder);  // "ASC" or "DESC"
	    params.put("offset",    offset);     // int
	    params.put("limit",     size);       // int

	    // 4) DAO 호출
	    List<bomDTO> select_bomList = b_dao.select_bomList(params);
	    int totalCount = b_dao.select_bomCount(params);

	    // 5) 전체 페이지 수 계산
	    int totalPages = (int) Math.ceil(totalCount / (double) size);

	    // 6) View로 보낼 값 세팅
	    m.addAttribute("select_bomList", select_bomList);
	    m.addAttribute("currentPage",    page);
	    m.addAttribute("pageSize",       size);
	    m.addAttribute("totalCount",     totalCount);
	    m.addAttribute("totalPages",     totalPages);
	    m.addAttribute("keyword",        keyword);
	    m.addAttribute("sortField",      sortField);
	    m.addAttribute("sortOrder",      sortOrder);
	    
	    //System.out.println(select_bomList);

	    return "bom/bom_list";
	}
	
	
   @GetMapping("/bom_edit")
   public String editBomForm(@RequestParam("bomCode") String bomCode, Model m) {
	   
	  //detail, edit 링크 전송
	  bomDTO bom= this.b_dao.select_bom_by_bom(bomCode);
	  m.addAttribute("bomCode", bomCode);
	  
	  // BOM 헤더만 조회
	    bomDTO header = this.b_dao.selectBomHeaderByCode(bomCode);
	   m.addAttribute("header", header); 
	   // BOM 디테일 조회(원자재)
	   List<bomDTO> detail = this.b_dao.edite_bom_detail(bomCode);//원자재 리스트는 여러개이므로 list
	    m.addAttribute("detail", detail);
	   
	 // System.out.println(detail);
	  	 
	   //bom 디테일 (자재)
	   
	   
	   
	   
	   //bom 같은 경우는 코드 번호를 따로 불러오기 때문에 bom 코드 기준으로 불려와줘야함ㄴ
	  /* 
	  List<bomDTO> select_header_list = this.b_dao.bom_item_type_y(); //완제품 
		m.addAttribute("select_header_list", select_header_list);
		//System.out.println(select_header_list);
		
		List<bomDTO> select_detail_list = this.b_dao.bom_item_type_j(); //원자재		
		m.addAttribute("select_detail_list", select_detail_list);
		//System.out.println(select_detail_list);
		*/
       return "bom/bom_edit"; // 수정화면 (HTML or Thymeleaf)
       
   }
   
   
   //BOM 수정   
   @PostMapping("/bom_modify")
   public String modifyBom(@ModelAttribute bomDTO dto, Model model) {
       int updatedCount = b_dao.modify_bom_detail(dto);

       if (updatedCount > 0) {
           System.out.println("BOM 수정 성공!");
       } else {
           System.out.println("BOM 수정 실패 또는 변경 없음.");
       }

       // model에 bomCode를 넣어 전달
       model.addAttribute("bomCode", dto.getBomHeaderId());

       // forward 방식으로 이동 (예: bom_detail.jsp)
       return "bom/bom_detail"; // → View Resolver가 이 경로의 JSP or Thymeleaf 찾아감
   }
  
   
   @PostMapping("/bom_deleteok")
   public String deleteBom(@RequestParam("bomCode") String bomCode, Model m) {
       // 1. 상세부터 삭제
       int deletedDetail = this.b_dao.delete_bom_detail(bomCode);

       // 2. 그 다음 헤더 삭제
       int deletedHeader = this.b_dao.delete_bom_header(bomCode);

       // 🔥 추가: 삭제 후 /bom_list로 리다이렉트
       return "redirect:/bom_list";
   }
   
   
   /*
 
   @PostMapping("/bom_deleteok")
   public String deleteBom(@RequestParam("bomCode") String bomCode, Model m) {
           // 1. 상세부터 삭제
           int deletedDetail = this.b_dao.delete_bom_detail(bomCode);

           // 2. 그 다음 헤더 삭제
           int deletedHeader = this.b_dao.delete_bom_header(bomCode);

           // 삭제 후 목록 다시 가져오기
           List<bomDTO> bomList = this.b_dao.selectBomList();
           m.addAttribute("bomList", bomList);
           
           
           // 🔥 추가: groupedBomMap 만들기
           Map<String, List<bomDTO>> groupedBomMap = bomList.stream()
               .collect(Collectors.groupingBy(b -> b.getBomCode()));
           m.addAttribute("groupedBomMap", groupedBomMap);

           // 메시지도 같이 넘기기
           m.addAttribute("msg", "BOM이 성공적으로 삭제되었습니다!");

           return "bom/bom_list";
   }
 
   */
 
   
  //BOM 삭제 
   @GetMapping("/bom_delete")
   public String deleteBom(@RequestParam("bomCode") String bomCode) {
       int deletedCount =b_dao.delete_bom_details(bomCode);

       if (deletedCount > 0) {
           System.out.println("BOM 삭제 완료");
       } else {
           System.out.println("삭제할 자재가 없거나 실패");
       }

       return "bom/bom_list"; // 목록 페이지로 이동
   }
   
  
  //등록할떄 값을 가져올 리스트 
  @GetMapping("/bom_write")
  public String bom_write(Model m) {	
	  
	  
	  List<bomDTO> item_bomok_List = this.b_dao.bom_item_list_y();
	  m.addAttribute("bom_item_y_list", item_bomok_List);
	  //완제품, 원자재 리스트 조회
	  /*
	  List<bomDTO> bom_item_y_list= this.b_dao.bom_item_type_y();
	  List<bomDTO> bom_item_j_list= this.b_dao.bom_item_type_j();

	  m.addAttribute("bom_item_y_list", bom_item_y_list);//완제품 
      m.addAttribute("bom_item_j_list", bom_item_j_list);//원자재 조회
      	  */
 	  System.out.println(item_bomok_List);
	  
  return "bom/bom_write";   
     }
  
  
  @PostMapping("/bom_writeok")
  public String bom_writeok(@ModelAttribute itemDTO dto, Model m) {
  
	  //저장 후 리스트 조회 
	  List<bomDTO> select_bomList = this.b_dao.selectBomList();
	  m.addAttribute("select_bomList", select_bomList);
	  m.addAttribute("msg", "등록이 완료되었습니다.");
	  return "bom/bom_list";   
  }
}
  
    

