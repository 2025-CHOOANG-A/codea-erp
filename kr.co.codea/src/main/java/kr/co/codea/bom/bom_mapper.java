package kr.co.codea.bom;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.co.codea.item.itemDTO;



@Mapper
public interface bom_mapper {

  //bom등록  	
	List<bomDTO> bom_item_type_y(); // 완제품
	List<bomDTO> bom_item_type_j(); // 자재
	
	
	
	
	/*등록하기 위한 필요한 리스트 */
	List<bomDTO> bom_item_list_y();//완제품
	
	//BOM 등록(bom_write)
	int insert_bom_header(bomDTO dto);
	int insert_bom_detail(bomDTO dto);
	
	 // 3) (선택) ITEM 조회
    Long selectItemIdByCode(String itemCode);

    // 4) (선택) 새 ITEM INSERT
    void insert_item_material(itemDTO itemDto);

	
	
	
	
	
	//BOM 주문목록
	List<bomDTO> selectBomList(); 
	
	//BOM 상세정보(디테일)
	//List<bomDTO> selectBomDetail();
	
   //BOM 상세정보
	List<bomDTO> selectBomProductByCode();//완제품
	List<bomDTO> selectBomMaterialList();//자재 리스트
	

	
	//BOM 수정파트
	//BOM 삭제
	 int delete_bom_details(@Param("bomCode") String bomCode);
	
	 /*bom 수정파트*/
	 
	//BOM 수정(원자재수정)	
	 int modify_bom_detail(bomDTO dto);
	 
	 //bom 헤더 단일 상세 정보전달 
      bomDTO selectBomHeaderByCode(String bomCode);
      
      
      //bom 헤더 단일 상세 정보전달 
      bomDTO select_bom_by_bom(String bomCode); //제품
      List<bomDTO> edite_bom_detail(String bomCode);//원자제(리스트가 여러개이기떄문)
      
      
      /*BOM삭제 파트 */
      
      //bom 삭제 
      int delete_bom_header(String bomCode);
      int delete_bom_detail(String bomCode);
      
      
      
	
}
