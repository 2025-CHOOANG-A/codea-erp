package kr.co.codea.bom;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import kr.co.codea.item.itemDTO;


public interface bom_service {

    // 1. BOM 등록 (헤더 + 디테일)
	//public List<bomDTO> insert_bom_header();
	//public List<bomDTO> insert_bom_detail();
	  int insert_bom_header(bomDTO dto);
	  int insert_bom_detail(bomDTO dto);
	  int insert_bom_details(List<bomDTO> detailList);
	  //public List<bomDTO> bom_item_list_y();//완제품 조회
	  
	//원자재 업데잍 하기 위한 조회
    //bomDTO selectItemIdByCode(String materialCode);
		String selectItemIdByCode(String materialCode);
		
	//<!-- 4. 디버깅용 - BOM_HEADER 존재 확인 -->
	  bomDTO checkBomHeaderExists(String bomHeaderId);		
	//<!-- 5. 디버깅용 - ITEM 존재 확인 -->
      bomDTO checkItemExists(String itemId);
	 
	
    // 2. 완제품 항목 조회
    public List<bomDTO> bom_item_type_y(); // itemType = "완제품"

    // 3. 자재 항목 조회
    public List<bomDTO> bom_item_type_j(); // itemType = "자재"
    
    public List<bomDTO> bom_item_list_y();
    
 
	//BOM 주문목록
    public List<bomDTO> selectBomList(); 
    
    //BOM 상세정보
    // public List<bomDTO> selectBomDetail(String bomCode);//인터페이슨
    
    // 상단: BOM 코드로 완제품 1건 조회
    List<bomDTO> selectBomHeaderByBomCode(@Param("bomCode") String bomCode);

    // 하단: BOM 코드로 자재목록 조회
    List<bomDTO> selectBomDetailByBomCode(@Param("bomCode") String bomCode);

    //bom 전체 삭제
	 int delete_bom_details(@Param("bomCode") String bomCode);
	//bom (원자재 수정)	
	 int modify_bom_detail(bomDTO dto);
        
	 //bom 헤더 단일 상세 정보전달 
	 bomDTO selectBomHeaderByCode(String bomCode);//제품
	public List<bomDTO> edite_bom_detail(String bomCode);
	 
	  /*BOM edit 파트*/
	 //bom 헤더 단일 상세 정보전달 
      public bomDTO select_bom_by_bom(String bomCode); //제품
     
   // ==========================================
   // 수정 기능을 위해 추가된 메서드들
   // ==========================================

   // BOM 상세 수정 (기존 에러 해결용)
   int edite_bom_detail_ok(bomDTO dto);

   // BOM 헤더 정보 수정
   int update_bom_header(bomDTO dto);

   // 자재 추가
   int add_bom_detail(bomDTO dto);

   // 선택된 자재들 삭제 (체크박스 다중 삭제용)
   int delete_bom_detail_by_material(Map<String, Object> params);

   // 단일 자재 삭제
   int delete_single_bom_detail(String bomCode, String materialCode);

   // 자재 검색 (모달에서 자재 선택용)
   List<bomDTO> search_materials(String keyword);

   // 수정용 BOM 전체 정보 조회
   bomDTO select_bom_for_edit(String bomCode);

   // 수정용 자재 목록 조회
   List<bomDTO> select_bom_materials_for_edit(String bomCode);
      
     
     /*BOM 전체 삭제 */
	   int delete_bom_header(String bomCode);
	   int delete_bom_detail(String bomCode);
	   
	   
	 /*BOM 페이징, 검색어, 오름차순, 내림차순*/  
	   
	   public List<bomDTO> select_bomList(Map<String, Object> params);
	   
	   int select_bomCount(Map<String, Object> params); //페이지 카운트
	   
	   public List<bomDTO> select_allBomList(Map<String, Object> params);
}
