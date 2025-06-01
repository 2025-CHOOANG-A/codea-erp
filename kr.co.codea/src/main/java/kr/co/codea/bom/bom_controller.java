package kr.co.codea.bom;

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
         
		  //bom 코드번호 기준으로 조회하게 하기 
		 m.addAttribute("selectedBomCode", bomCode);
		
		  // ⛳️ 이 부분 추가! 단일 BOM 헤더만 조회
		    bomDTO header = this.b_dao.selectBomHeaderByCode(bomCode);
		    m.addAttribute("header", header); 
	//System.out.println(header);
		 
        // 자재 , 원자재로 넘기기 		
		List<bomDTO> select_header_list = this.b_dao.bom_item_type_y(); //완제품 
		m.addAttribute("select_header_list", select_header_list);
		
		List<bomDTO> select_detail_list = this.b_dao.bom_item_type_j(); //원자재		
		m.addAttribute("select_detail_list", select_detail_list);
		
		//System.out.println(select_header_list);
		//System.out.println(select_detail_list);
	    return "bom/bom_detail"; 
	}
		
	
  //BOM 목록
   @GetMapping("/bom_list")
   public String bom_list( Model m ){
	    //bom 코드번호 기준으로 조회하게 하기 
	  // m.addAttribute("selectedBomCode", bomCode);
	   
       List<bomDTO> select_bomList = this.b_dao.selectBomList();
       m.addAttribute("select_bomList", select_bomList); // 단순 리스트
       //m.addAttribute("header", bomCode);
       return "bom/bom_list";
   }
   
  
   @GetMapping("/bom/bom_edit")
   public String editBomForm( @RequestParam("bomCode") String bomCode, Model m) {
	
	  
       List<bomDTO> headerList = b_dao.selectBomHeaderByBomCode(bomCode);
       List<bomDTO> detailList = b_dao.selectBomDetailByBomCode(bomCode);

       m.addAttribute("select_header_list", headerList);
       m.addAttribute("select_detail_list", detailList);
       m.addAttribute("bomCode2", bomCode); // 필요 시 추가

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
       model.addAttribute("bomCode", dto.getBomCode());

       // forward 방식으로 이동 (예: bom_detail.jsp)
       return "bom/bom_detail"; // → View Resolver가 이 경로의 JSP or Thymeleaf 찾아감
   }
  
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
	  //완제품, 원자재 리스트 조회
	  List<bomDTO> bom_item_y_list= this.b_dao.bom_item_type_y();
	  List<bomDTO> bom_item_j_list= this.b_dao.bom_item_type_j();
	  
	  m.addAttribute("bom_item_y_list", bom_item_y_list);//완제품 , 워낮제
      m.addAttribute("bom_item_j_list", bom_item_j_list);//원자재 조회
 	  System.out.println(bom_item_j_list);
	  
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
  
    

