package kr.co.codea.bom;

import java.util.ArrayList;
import java.util.HashMap;
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

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;




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
	    }`
	    
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
	        @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
	        @RequestParam(value = "page", required = false, defaultValue = "1") String pageStr,
	        @RequestParam(value = "size", required = false, defaultValue = "3") String sizeStr,
	        @RequestParam(value = "sortField", required = false, defaultValue = "bomCode") String sortField,
	        @RequestParam(value = "sortOrder", required = false, defaultValue = "ASC") String sortOrder,
	        Model m
	    ) {
	        // 기존 로직 그대로 유지
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

	       int offset = (page - 1) * size;

	        Map<String, Object> params = new HashMap<>();
	        params.put("keyword", keyword);
	        params.put("sortField", sortField);
	        params.put("sortOrder", sortOrder);
	        //params.put("offset", offset);
	        params.put("limit", size);

	        // 기존 DAO 호출 그대로
	        List<bomDTO> select_bomList = b_dao.select_bomList(params);
	        int totalCount = b_dao.select_bomCount(params);

	        int totalPages = (int) Math.ceil(totalCount / (double) size);

	        // 기존 변수명 그대로 Model에 추가
	        m.addAttribute("select_bomList", select_bomList);
	        m.addAttribute("currentPage", page);
	        m.addAttribute("pageSize", size);
	        m.addAttribute("totalCount", totalCount);
	        m.addAttribute("totalPages", totalPages);
	        m.addAttribute("keyword", keyword);
	        m.addAttribute("sortField", sortField);
	        m.addAttribute("sortOrder", sortOrder);

	        // Thymeleaf 템플릿으로 변경
	        return "bom/bom_list";
	    }
	
	
	 @GetMapping("/bom_edit")
		public String editBomForm(@RequestParam("bomCode") String bomCode, Model m) throws JsonProcessingException {

		    // 기존 코드
		    bomDTO bom = this.b_dao.select_bom_by_bom(bomCode);
		    bomDTO header = this.b_dao.selectBomHeaderByCode(bomCode);
		    List<bomDTO> detail = this.b_dao.edite_bom_detail(bomCode);
		    List<bomDTO> bom_item_j_list= this.b_dao.bom_item_type_j();//원자재 자재추가 항목코드 추가 


		    // 기존 모델 추가
		    m.addAttribute("bomCode", bomCode);
		    m.addAttribute("header", header);
		    m.addAttribute("detail", detail);
		    m.addAttribute("bom_item_j_list", bom_item_j_list);//원자재 조회
		    
		    System.out.println("header 결과: " + (header != null ? "정상" : "null"));
		    System.out.println("detail 결과: " + (detail != null ? detail.size() + "개" : "null"));
		    System.out.println(bom_item_j_list);

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
		  // System.out.println(detail);
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

		// BOM 상세 자재 추가 (JSON 방식으로 수정)
		@PostMapping("/addMaterial")
		@ResponseBody
		public Map<String, Object> addBomMaterial(@RequestBody Map<String, Object> requestData) {
		    Map<String, Object> result = new HashMap<>();
		    
		    try {
		        // JSON 데이터에서 값 추출
		        String bomCode = (String) requestData.get("bomCode");
		        String materialCode = (String) requestData.get("materialCode");
		        String materialName = (String) requestData.get("materialName");
		        String spec = (String) requestData.get("spec");
		        String unit = (String) requestData.get("unit");
		        Object priceObj = requestData.get("price");
		        Object quantityObj = requestData.get("quantity");
		        Object lossRateObj = requestData.get("lossRate");
		        
		        // 숫자 변환
		        Integer price = convertToInteger(priceObj);
		        Integer quantity = convertToInteger(quantityObj);
		        Integer lossRate = convertToInteger(lossRateObj);
		        
		        System.out.println("자재 추가 요청:");
		        System.out.println("bomCode: " + bomCode);
		        System.out.println("materialCode: " + materialCode);
		        System.out.println("materialName: " + materialName);
		        System.out.println("quantity: " + quantity);
		        System.out.println("price: " + price);
		        
		        // bomDTO 생성
		        bomDTO bomDTO = new bomDTO();
		        bomDTO.setBomCode(bomCode);
		        bomDTO.setMaterialCode(materialCode);
		        bomDTO.setMaterialName(materialName);
		        bomDTO.setSpec(spec);
		        bomDTO.setUnitName(unit);
		        bomDTO.setQuantity(quantity != null ? quantity : 1);
		        bomDTO.setPrice(price != null ? price : 0);
		        bomDTO.setLossRate(lossRate != null ? lossRate : 0);
		        
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
		        System.err.println("자재 추가 오류 상세: " + e.getMessage());
		    }
		    
		    return result;
		}

		//자제건당 삭제 잘됨 
		@PostMapping("/deleteMaterials")
		@ResponseBody
		public Map<String, Object> deleteBomMaterials(
		    @RequestParam("bomCode") String bomCode,           // ← 명시적으로 이름 지정
		    @RequestParam("materialCodes") List<String> materialCodes) {  // ← 명시적으로 이름 지정
		    
		    Map<String, Object> result = new HashMap<>();
		    
		    try {
		        System.out.println("받은 bomCode: " + bomCode);
		        System.out.println("받은 materialCodes: " + materialCodes);
		        
		        // Mapper 호출
		        Map<String, Object> params = new HashMap<>();
		        params.put("bomCode", bomCode);
		        params.put("materialCodes", materialCodes);
		        
		        int deleteResult = this.b_dao.delete_bom_detail_by_material(params);
		        
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
		
		// BOM 저장 (화면에 남은 자재들만 DB에 반영) - 저장 후 바로 리스트로 리다이렉트
		@PostMapping("/bom_editok")
		@Transactional
		public String saveBom(@RequestParam("bomCode") String bomCode,
		                     @RequestParam("materialsJson") String materialsJson) {
		    
		    try {
		        System.out.println("저장할 bomCode: " + bomCode);
		        System.out.println("저장할 materialsJson: " + materialsJson);
		        
		        // 1. 기존 자재 목록 전체 삭제
		        int deleteResult = b_dao.delete_bom_details(bomCode);
		        System.out.println("기존 자재 삭제 결과: " + deleteResult + "건");
		        
		        // 2. 화면에 남아있는 자재들만 다시 등록
		        if (materialsJson != null && !materialsJson.trim().isEmpty() && !materialsJson.equals("[]")) {
		            ObjectMapper mapper = new ObjectMapper();
		            List<Map<String, Object>> materialsList = mapper.readValue(materialsJson, 
		                new TypeReference<List<Map<String, Object>>>() {});
		            
		            if (!materialsList.isEmpty()) {
		                // BOM_HEADER_ID 조회
		                bomDTO headerInfo = b_dao.selectBomHeaderByCode(bomCode);
		                String bomHeaderId = headerInfo.getBomHeaderId();
		                
		                // 자재들을 하나씩 등록
		                for (Map<String, Object> materialMap : materialsList) {
		                    bomDTO material = new bomDTO();
		                    material.setBomHeaderId(bomHeaderId);
		                    material.setMaterialCode((String) materialMap.get("materialCode"));
		                    material.setQuantity(Integer.parseInt(materialMap.get("quantity").toString()));
		                    material.setPrice(Integer.parseInt(materialMap.get("price").toString()));
		                    
		                    // 기존 매퍼 사용해서 INSERT
		                    b_dao.modify_bom_detail(material);
		                }
		                
		                System.out.println("새로운 자재 등록 결과: " + materialsList.size() + "건");
		            }
		        }
		        
		        System.out.println("BOM 저장 완료!");
		        
		    } catch (Exception e) {
		        System.err.println("BOM 저장 중 오류 발생: " + e.getMessage());
		        e.printStackTrace();
		        // 트랜잭션 롤백됨
		    }
		    
		    // 저장 완료 후 바로 BOM 리스트로 리다이렉트
		    return "redirect:/bom/bom_list";
		}
	   
	 
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

	           return "redirect:/bom/bom_list";
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
		  public String bom_write( Model m) {	
			  
			  //자재추가(ITEM 항목 불러옴)
			  //List<bomDTO> item_bom = this.b_dao.bom_item_list(itemId);
			  
			  //완제품, 원자재 리스트 조회

			  List<bomDTO> bom_item_y_list= this.b_dao.bom_item_type_y();
			  List<bomDTO> bom_item_j_list= this.b_dao.bom_item_type_j();

			  m.addAttribute("bom_item_y_list", bom_item_y_list);//완제품 
		      m.addAttribute("bom_item_j_list", bom_item_j_list);//원자재 조회
		      System.out.println(bom_item_y_list);
		      System.out.println(bom_item_j_list);

			  
		      return "bom/bom_write";   
		      
		     }
		   
		  @PostMapping("/bom_save_details")
		  @ResponseBody
		  public Map<String, Object> saveBomDetails(@RequestBody Map<String, Object> requestData) {
		      Map<String, Object> result = new HashMap<>();
		      
		      try {
		          String bomHeaderId = (String) requestData.get("bomHeaderId");
		          System.out.println("받은 bomHeaderId: [" + bomHeaderId + "]");
		          @SuppressWarnings("unchecked")
		          List<Map<String, Object>> materialsData = (List<Map<String, Object>>) requestData.get("materials");
		          
		          if (bomHeaderId == null || bomHeaderId.trim().isEmpty()) {
		              result.put("success", false);
		              result.put("message", "BOM 헤더 ID가 누락되었습니다.");
		              return result;
		          }
		          
		          if (materialsData == null || materialsData.isEmpty()) {
		              result.put("success", false);
		              result.put("message", "자재 정보가 누락되었습니다.");
		              return result;
		          }
		          
		          int successCount = 0;
		          int failCount = 0;
		          
		          for (int i = 0; i < materialsData.size(); i++) {
		              Map<String, Object> materialData = materialsData.get(i);
		              
		              try {
		                  String materialCode = (String) materialData.get("materialCode");
		                  String itemId = (String) materialData.get("itemId");
		                  String childId = (String) materialData.get("childId");
		                  
		                  String finalChildIdStr = (childId != null && !childId.trim().isEmpty()) ? childId : itemId;
		                  
		                  if (finalChildIdStr == null || finalChildIdStr.trim().isEmpty()) {
		                      if (materialCode != null && !materialCode.trim().isEmpty()) {
		                          try {
		                              String lookedUpItemId = b_dao.selectItemIdByCode(materialCode);
		                              if (lookedUpItemId != null && !lookedUpItemId.trim().isEmpty()) {
		                                  finalChildIdStr = lookedUpItemId;
		                              } else {
		                                  System.err.println("자재[" + i + "] itemId 조회 실패 - materialCode: " + materialCode);
		                              }
		                          } catch (Exception dbException) {
		                              System.err.println("자재[" + i + "] DB 조회 예외 - materialCode: " + materialCode + ", 오류: " + dbException.getMessage());
		                          }
		                      } else {
		                          System.err.println("자재[" + i + "] materialCode가 null/빈값입니다.");
		                      }
		                  }
		                  
		                  // childId 숫자 형식 검증
		                  if (finalChildIdStr != null && !finalChildIdStr.trim().isEmpty()) {
		                      try {
		                          // 숫자인지 검증만 하고 String으로 유지
		                          Long.parseLong(finalChildIdStr.trim());
		                      } catch (NumberFormatException e) {
		                          System.err.println("자재[" + i + "] childId 숫자 형식 오류 - childId: " + finalChildIdStr + ", 오류: " + e.getMessage());
		                          failCount++;
		                          continue;
		                      }
		                  } else {
		                      System.err.println("자재[" + (i+1) + "] finalChildId 누락 - materialCode: " + materialCode);
		                      failCount++;
		                      continue;
		                  }
		                  
		                  Integer quantity = convertToInteger(materialData.get("quantity"));
		                  Integer price = convertToInteger(materialData.get("price"));
		                  
		                  bomDTO detailDTO = new bomDTO();
		                  detailDTO.setBomHeaderId(bomHeaderId);
		                  detailDTO.setChildId(finalChildIdStr.trim()); // String으로 설정 (DB에서 NUMBER로 변환)
		                  detailDTO.setQuantity(quantity != null ? quantity : 1);
		                  detailDTO.setPrice(price != null ? price : 0);
		                  
		                  int detailResult = this.b_dao.insert_bom_detail(detailDTO);
		                  
		                  if (detailResult > 0) {
		                      successCount++;
		                  } else {
		                      failCount++;
		                      System.err.println("자재[" + i + "] 저장 실패 - materialCode: " + materialCode);
		                  }
		                  
		              } catch (Exception e) {
		                  failCount++;
		                  System.err.println("자재[" + i + "] 처리 중 예외: " + e.getMessage());
		              }
		          }
		          
		          if (successCount > 0) {
		              result.put("success", true);
		              result.put("message", "자재 저장 완료: 성공 " + successCount + "개, 실패 " + failCount + "개");
		              result.put("savedCount", successCount);
		              result.put("failedCount", failCount);
		              result.put("totalCount", materialsData.size());
		          } else {
		              result.put("success", false);
		              result.put("message", "모든 자재 저장 실패 (" + failCount + "개 실패)");
		              result.put("savedCount", 0);
		              result.put("failedCount", failCount);
		              result.put("totalCount", materialsData.size());
		          }
		          
		      } catch (Exception e) {
		          System.err.println("자재 저장 오류: " + e.getMessage());
		          result.put("success", false);
		          result.put("message", "자재 저장 오류: " + e.getMessage());
		      }
		      
		      return result;
		  }

@PostMapping("/bom_save_header")
@ResponseBody
public Map<String, Object> saveBomHeader(@RequestBody Map<String, Object> requestData) {
    Map<String, Object> result = new HashMap<>();
    
    try {
        String bomCode = (String) requestData.get("bomCode");
        String itemId = (String) requestData.get("itemId");
        String version = (String) requestData.get("version");
        String description = (String) requestData.get("description");
        
        if (itemId == null || itemId.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "완제품 ID가 누락되었습니다.");
            return result;
        }
        
        
        bomDTO headerDTO = new bomDTO();
        headerDTO.setItemId(itemId);
        headerDTO.setVersion(version != null && !version.trim().isEmpty() ? version : "1.0");
        headerDTO.setDescription(description != null && !description.trim().isEmpty() ? description : "신규 등록");
        
        int headerResult = this.b_dao.insert_bom_header(headerDTO);
        
        if (headerResult > 0 && headerDTO.getBomHeaderId() != null && !headerDTO.getBomHeaderId().trim().isEmpty()) {
            result.put("success", true);
            result.put("message", "BOM 제품 저장 완료");
            result.put("bomHeaderId", headerDTO.getBomHeaderId());
            result.put("bomCode", bomCode);
        } else {
            result.put("success", false);
            result.put("message", "BOM 제품 저장 실패");
            System.err.println("헤더 저장 실패 - headerResult: " + headerResult + ", bomHeaderId: " + headerDTO.getBomHeaderId());
        }
        
    } catch (Exception e) {
        System.err.println("제품 저장 오류: " + e.getMessage());
        result.put("success", false);
        result.put("message", "제품 저장 오류: " + e.getMessage());
    }
    
    return result;
}

private Integer convertToInteger(Object value) {
    if (value == null) {
        return null;
    }
    
    try {
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            String strValue = ((String) value).trim();
            if (strValue.isEmpty()) {
                return null;
            }
            return Integer.parseInt(strValue);
        } else if (value instanceof Double) {
            return ((Double) value).intValue();
        } else if (value instanceof Float) {
            return ((Float) value).intValue();
        } else if (value instanceof Long) {
            return ((Long) value).intValue();
        }
    } catch (NumberFormatException e) {
        System.err.println("숫자 변환 실패: " + value + " (" + value.getClass().getSimpleName() + ")");
    }
    
    return null;
}
		  
	  /*
	  @PostMapping("/bom_writeok")
	  @ResponseBody
	  @Transactional
	  public Map<String, Object> bomWriteOk(@RequestBody bomDTO bomData) {
	      System.out.println("▶▶▶ 들어온 DTO: " + bomData);
	      
	      Map<String, Object> result = new HashMap<>();
	      
	      try {
	          // 필수 필드 검증
	          if (bomData.getItemId() == null || bomData.getItemId().trim().isEmpty()) {
	              result.put("success", false);
	              result.put("message", "완제품 ID가 누락되었습니다.");
	              return result;
	          }
	          
	          if (bomData.getMaterials() == null || bomData.getMaterials().isEmpty()) {
	              result.put("success", false);
	              result.put("message", "자재 정보가 누락되었습니다.");
	              return result;
	          }
	          
	          // 1. BOM 헤더 저장
	          System.out.println("▶▶▶ BOM 헤더 저장 시작");
	          this.b_dao.insert_bom_header(bomData);
	          System.out.println("▶▶▶ 생성된 bomHeaderId: " + bomData.getBomHeaderId());
	          
	          // bomHeaderId 생성 확인
	          if (bomData.getBomHeaderId() == null || bomData.getBomHeaderId().trim().isEmpty()) {
	              throw new RuntimeException("BOM Header ID 생성 실패 - 시퀀스를 확인하세요");
	          }
	          
	          // 2. 자재들에 bomHeaderId 설정
	          for (bomDTO material : bomData.getMaterials()) {
	              material.setBomHeaderId(bomData.getBomHeaderId());
	              
	              // 자재 필드 검증
	              if (material.getChildId() == null || material.getChildId().trim().isEmpty()) {
	                  throw new RuntimeException("자재의 childId가 누락되었습니다: " + material);
	              }
	              
	              System.out.println("▶▶▶ 자재: bomHeaderId=" + material.getBomHeaderId() + 
	                               ", childId=" + material.getChildId() + 
	                               ", quantity=" + material.getQuantity() + 
	                               ", price=" + material.getPrice());
	          }
	          
	          // 3. BOM 디테일들 저장 (반환값 확인)
	          System.out.println("▶▶▶ BOM 디테일 저장 시작. 자재 개수: " + bomData.getMaterials().size());
	          int insertedCount = this.b_dao.insert_bom_details(bomData.getMaterials());
	          System.out.println("▶▶▶ 디테일 저장 결과: " + insertedCount + "건 처리됨");
	          
	          if (insertedCount <= 0) {
	              throw new RuntimeException("BOM 디테일 저장 실패: 처리된 건수가 0입니다");
	          }
	          
	          result.put("success", true);
	          result.put("message", "BOM이 성공적으로 등록되었습니다. (헤더 1건, 디테일 " + insertedCount + "건)");
	          
	      } catch (Exception e) {
	          System.err.println("▶▶▶ BOM 등록 오류: " + e.getMessage());
	          e.printStackTrace();
	          
	          result.put("success", false);
	          result.put("message", "BOM 등록 실패: " + e.getMessage());
	      }
	      
	      return result;
	  }
	*/
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
	  
	    