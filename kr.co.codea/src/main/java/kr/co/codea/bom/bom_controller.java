package kr.co.codea.bom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
         
		
		 //detail, edit 링크 전송
		  bomDTO bom= this.b_dao.select_bom_by_bom(bomCode);
		  m.addAttribute("bomCode", bomCode);
		
		  // ⛳️ 이 부분 추가! 단일 BOM 헤더만 조회
		    bomDTO header = this.b_dao.selectBomHeaderByCode(bomCode);
		   m.addAttribute("header", header); 
	     System.out.println(header);
		 
        // 자재 , 원자재로 넘기기 		
		List<bomDTO> select_header_list = this.b_dao.bom_item_type_y(); //완제품 
		m.addAttribute("select_header_list", select_header_list);
		
		List<bomDTO> select_detail_list = this.b_dao.bom_item_type_j(); //원자재		
		m.addAttribute("select_detail_list", select_detail_list);
		
		System.out.println(select_detail_list);
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
	
	// ============= 1. 컨트롤러 수정 =============
	/*
	@GetMapping("/bom_list")
	public String bom_list(
	    @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
	    @RequestParam(value = "page", required = false, defaultValue = "1") String pageStr,
	    @RequestParam(value = "size", required = false, defaultValue = "10") String sizeStr,
	    @RequestParam(value = "sortField", required = false, defaultValue = "bomCode") String sortField,
	    @RequestParam(value = "sortOrder", required = false, defaultValue = "ASC") String sortOrder,
	    Model m
	) {
	    
	    // 1) 페이지 파라미터 처리
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
	        if (size < 1) size = 10;
	    } catch (NumberFormatException e) {
	        size = 10;
	    }
	    
	    // 2) 먼저 전체 데이터를 조회 (페이징 없이)
	    Map<String, Object> searchParams = new HashMap<>();
	    searchParams.put("keyword", keyword);
	    searchParams.put("sortField", sortField);
	    searchParams.put("sortOrder", sortOrder);
	    
	    List<bomDTO> allBomList = b_dao.select_allBomList(searchParams);
	    
	    // 3) 컨트롤러에서 그룹화 처리
	    Map<String, GroupedBomDTO> groupedMap = new LinkedHashMap<>();
	    
	    for (bomDTO bom : allBomList) {
	        String key = bom.getBomCode() + "_" + bom.getProductCode();
	        
	        if (!groupedMap.containsKey(key)) {
	            GroupedBomDTO grouped = new GroupedBomDTO();
	            grouped.setBomCode(bom.getBomCode());
	            grouped.setProductCode(bom.getProductCode());
	            grouped.setProductName(bom.getProductName());
	            grouped.setReMark(bom.getReMark());
	            grouped.setMaterials(new ArrayList<>());
	            groupedMap.put(key, grouped);
	        }
	        
	        // 자재 정보 추가
	        MaterialDTO material = new MaterialDTO();
	        material.setMaterialCode(bom.getMaterialCode());
	        material.setMaterialName(bom.getMaterialName());
	        material.setSpec(bom.getSpec());
	        material.setUnitUnicode(bom.getUnitUnicode());
	        material.setUnitName2(bom.getUnitName2());
	        material.setPrice(bom.getPrice());
	        material.setQuantity(bom.getQuantity());
	        
	        groupedMap.get(key).getMaterials().add(material);
	    }
	    
	    // 4) 그룹화된 리스트로 변환
	    List<GroupedBomDTO> groupedBomList = new ArrayList<>(groupedMap.values());
	    
	    // 5) 페이징 처리 (그룹화된 데이터 기준)
	    int totalCount = groupedBomList.size();
	    int totalPages = (int) Math.ceil(totalCount / (double) size);
	    
	    int startIndex = (page - 1) * size;
	    int endIndex = Math.min(startIndex + size, totalCount);
	    
	    List<GroupedBomDTO> pagedBomList = new ArrayList<>();
	    if (startIndex < totalCount) {
	        pagedBomList = groupedBomList.subList(startIndex, endIndex);
	    }
	    
	    // 6) View로 데이터 전달
	    m.addAttribute("select_bomList", pagedBomList);
	    m.addAttribute("currentPage", page);
	    m.addAttribute("pageSize", size);
	    m.addAttribute("totalCount", totalCount);
	    m.addAttribute("totalPages", totalPages);
	    m.addAttribute("keyword", keyword);
	    m.addAttribute("sortField", sortField);
	    m.addAttribute("sortOrder", sortOrder);
	    
	    return "bom/bom_list";
	}
	*/
	
	
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
	public String editBomForm(@RequestParam("bomCode") String bomCode, Model m) throws JsonProcessingException {

	    // 기존 코드
	    bomDTO bom = this.b_dao.select_bom_by_bom(bomCode);
	    bomDTO header = this.b_dao.selectBomHeaderByCode(bomCode);
	    List<bomDTO> detail = this.b_dao.edite_bom_detail(bomCode);

	    // 기존 모델 추가
	    m.addAttribute("bomCode", bomCode);
	    m.addAttribute("header", header);
	    m.addAttribute("detail", detail);
	    
	    System.out.println("header 결과: " + (header != null ? "정상" : "null"));
	    System.out.println("detail 결과: " + (detail != null ? detail.size() + "개" : "null"));
	    

	    // BomDTO 리스트를 화면에 필요한 필드만 추출해서 Map 리스트로 변환
	    List<Map<String, Object>> materialsList = new ArrayList<>();
	    
	    for(bomDTO item : detail) {
	        Map<String, Object> material = new HashMap<>();
	        material.put("bomCode", item.getBomCode());           // BOM코드
	        material.put("productCode", item.getMaterialCode());   // 자재코드
	        material.put("productName", item.getMaterialName());   // 자재명
	        material.put("spec", item.getSpec());                 // 규격
	        material.put("unitName", item.getUnitName());         // 단위
	        material.put("description", item.getReMark());        // 설명
	        material.put("quantity", item.getQuantity());         // 수량
	        
	        materialsList.add(material);
	    }

	    // JavaScript 객체용 JSON 생성
	    Map<String, Object> bomDetail = new HashMap<>();
	    bomDetail.put("bomCode", header.getBomCode());
	    bomDetail.put("productCode", header.getProductCode());
	    bomDetail.put("productName", header.getProductName());
	    bomDetail.put("spec", header.getSpec());
	    bomDetail.put("unitCode", header.getUnitCode());
	    bomDetail.put("note", header.getDescription());
	    bomDetail.put("materials", materialsList);  // 변환된 리스트 사용

	    //System.out.println(materialsList);
	    ObjectMapper mapper = new ObjectMapper();
	    String bomDetailJson = mapper.writeValueAsString(bomDetail);
	    m.addAttribute("bomDetailJson", bomDetailJson);

	    //System.out.println(header);
	   System.out.println(detail);
	    //System.out.println(bomCode);
	    
	   //System.out.println("=== JSON 확인 ===");
	    //System.out.println("bomDetailJson: " + bomDetailJson);
	    //System.out.println("bomDetailJson length: " + bomDetailJson.length());

	    return "bom/bom_edit";
	}
	
	// BOM 상세 자재 수정 (AJAX)
	@PostMapping("/updateMaterial")
	@ResponseBody
	public Map<String, Object> updateBomMaterial(@RequestBody bomDTO bomDTO) {
	    Map<String, Object> result = new HashMap<>();
	    
	    try {
	        int updateResult = this.b_dao.edite_bom_detail_ok(bomDTO);
	        
	        if (updateResult > 0) {
	            result.put("success", true);
	            result.put("message", "자재 정보가 성공적으로 수정되었습니다.");
	        } else {
	            result.put("success", false);
	            result.put("message", "수정할 자재를 찾을 수 없습니다.");
	        }
	        
	    } catch (Exception e) {
	        result.put("success", false);
	        result.put("message", "자재 수정 중 오류가 발생했습니다: " + e.getMessage());
	        e.printStackTrace();
	    }
	    
	    return result;
	}

	// BOM 상세 자재 추가 (AJAX)
	@PostMapping("/addMaterial")
	@ResponseBody
	public Map<String, Object> addBomMaterial(@RequestBody bomDTO bomDTO) {
	    Map<String, Object> result = new HashMap<>();
	    
	    try {
	        int addResult = this.b_dao.add_bom_detail(bomDTO);
	        
	        if (addResult > 0) {
	            result.put("success", true);
	            result.put("message", "자재가 성공적으로 추가되었습니다.");
	        } else {
	            result.put("success", false);
	            result.put("message", "자재 추가에 실패했습니다.");
	        }
	        
	    } catch (Exception e) {
	        result.put("success", false);
	        result.put("message", "자재 추가 중 오류가 발생했습니다: " + e.getMessage());
	        e.printStackTrace();
	    }
	    
	    return result;
	}

	// 선택된 자재들 삭제 (AJAX)
	@PostMapping("/deleteMaterials")
	@ResponseBody
	public Map<String, Object> deleteBomMaterials(@RequestParam String bomCode,
	                                             @RequestParam List<String> materialCodes) {
	    Map<String, Object> result = new HashMap<>();
	    
	    try {
	        int deleteResult = this.b_dao.delete_bom_detail_by_materials(bomCode, materialCodes);
	        
	        if (deleteResult > 0) {
	            result.put("success", true);
	            result.put("message", "선택된 자재가 성공적으로 삭제되었습니다.");
	        } else {
	            result.put("success", false);
	            result.put("message", "삭제할 자재를 찾을 수 없습니다.");
	        }
	        
	    } catch (Exception e) {
	        result.put("success", false);
	        result.put("message", "자재 삭제 중 오류가 발생했습니다: " + e.getMessage());
	        e.printStackTrace();
	    }
	    
	    return result;
	}

	// 자재 검색 (모달용 AJAX)
	@GetMapping("/searchMaterials")
	@ResponseBody
	public List<bomDTO> searchMaterials(@RequestParam(required = false) String keyword) {
	    try {
	        if (keyword == null || keyword.trim().isEmpty()) {
	            return this.b_dao.bom_item_list_y(); // 전체 자재 목록
	        } else {
	            return this.b_dao.search_materials(keyword); // 검색된 자재 목록
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        return new ArrayList<>(); // 에러 시 빈 리스트 반환
	    }
	}

	// 페이지 새로고침 없이 자재 목록 조회 (AJAX)
	@GetMapping("/getMaterials")
	@ResponseBody
	public List<bomDTO> getBomMaterials(@RequestParam String bomCode) {
	    try {
	        return this.b_dao.edite_bom_detail(bomCode);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return new ArrayList<>();
	    }
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
  public String bom_write( Model m) {	
	  
	  //자재추가(ITEM 항목 불러옴)
	  //List<bomDTO> item_bom = this.b_dao.bom_item_list(itemId);
	  
	  //완제품, 원자재 리스트 조회

	  List<bomDTO> bom_item_y_list= this.b_dao.bom_item_list_y();
	  List<bomDTO> bom_item_j_list= this.b_dao.bom_item_type_j();

	  m.addAttribute("bom_item_y_list", bom_item_y_list);//완제품 
      m.addAttribute("bom_item_j_list", bom_item_j_list);//원자재 조회
      System.out.println(bom_item_y_list);
       System.out.println(bom_item_j_list);
	  
  return "bom/bom_write";   
     }
  
  
  @PostMapping("/bom_writeok")
  @ResponseBody
  @Transactional 
  public Map<String, Object> bomWriteOk(@RequestBody bomDTO bomData) {
      Map<String, Object> result = new HashMap<>();
      try {
          // 1. BOM 헤더 저장
          this.b_dao.insert_bom_header(bomData);
          
          // 2. BOM 디테일들 저장 (리스트로 한 번에)
          if (bomData.getMaterials() != null && !bomData.getMaterials().isEmpty()) {
              this.b_dao.insert_bom_details(bomData.getMaterials());
          }
          
          result.put("success", true);
          result.put("message", "BOM이 성공적으로 등록되었습니다.");
          
      } catch (Exception e) {
          result.put("success", false);
          result.put("message", "BOM 등록 실패: " + e.getMessage());
          e.printStackTrace(); // 에러 로그 출력
      }
      return result;
  }
  
  /*
  @PostMapping("/bom_writeok")
  public String bom_writeok(@ModelAttribute bomDTO dto, RedirectAttributes ra) {
      
      try {
          // 1. Header 등록 (bomHeaderId 자동 생성됨)
          int headerResult = this.b_dao.insert_bom_header(dto);
          
          if (headerResult > 0) {
              // 2. Detail 등록 (여러건 한번에)
              if (dto.getDetailList() != null && !dto.getDetailList().isEmpty()) {
                  
                  // 모든 detail에 생성된 bomHeaderId 설정
                  for (bomDTO detail : dto.getDetailList()) {
                      detail.setBomHeaderId(dto.getBomHeaderId());
                  }
                  
                  // 한번에 여러건 등록
                  this.b_dao.insert_bom_details(dto.getDetailList());
              }
              
              ra.addFlashAttribute("msg", "BOM 등록이 완료되었습니다.");
          } else {
              ra.addFlashAttribute("msg", "등록에 실패했습니다.");
          }
          
      } catch (Exception e) {
          e.printStackTrace();
          ra.addFlashAttribute("msg", "오류: " + e.getMessage());
      }
      
      return "redirect:/bom/bom_list";
  }
  */
  
  /*
  @PostMapping("/bom_writeok")
  public String bom_writeok(@ModelAttribute itemDTO dto, Model m) {
  
	  //저장 후 리스트 조회 
	  List<bomDTO> select_bomList = this.b_dao.selectBomList();
	  m.addAttribute("select_bomList", select_bomList);
	  m.addAttribute("msg", "등록이 완료되었습니다.");
	  return "bom/bom_list";   
  }
  */

  
  
  
}
  
    

