package kr.co.codea.bom;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import kr.co.codea.item.itemDTO;


public interface bom_service {

    // 1. BOM 등록 (헤더 + 디테일)
	//public List<bomDTO> insert_bom_header();
	//public List<bomDTO> insert_bom_detail();
	  int insert_bom_header(bomDTO dto);
	  int insert_bom_detail(bomDTO dto);
	
    // 2. 완제품 항목 조회
    public List<bomDTO> bom_item_type_y(); // itemType = "완제품"

    // 3. 자재 항목 조회
    public List<bomDTO> bom_item_type_j(); // itemType = "자재"
	
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
	 bomDTO selectBomHeaderByCode(String bomCode);
	
	
	 //bom 헤더 단일 상세 정보전달 
      public bomDTO select_bom_by_bom(String bomCode);
     
     /*BOM 전체 삭제 */
	   int delete_bom_header(String bomCode);
	   int delete_bom_detail(String bomCode);
	   
	 
	 
}
