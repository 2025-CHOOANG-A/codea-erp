package kr.co.codea.bom;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;



@Mapper
public interface bom_mapper {

  //bom등록  	
	List<bomDTO> bom_item_type_y(); // 완제품
	List<bomDTO> bom_item_type_j(); // 자재
	
	//BOM 주문목록
	List<bomDTO> selectBomList(); 
	
	//BOM 상세정보(디테일)
	//List<bomDTO> selectBomDetail();
	
   //BOM 상세정보
	List<bomDTO> selectBomProductByCode();//완제품
	List<bomDTO> selectBomMaterialList();//자재 리스트
	
	//BOM 등록(bom_write)
	int insert_bom_header(bomDTO dto);
	int insert_bom_detail(bomDTO dto);
	
	//BOM 수정파트
	//BOM 삭제
	 int delete_bom_details(@Param("bomCode") String bomCode);
	
	//BOM 수정(원자재수정)	
	 int modify_bom_detail(bomDTO dto);
	 
	 //bom 헤더 단일 상세 정보전달 
      bomDTO selectBomHeaderByCode(String bomCode);
	
      //bom 헤더 단일 상세 정보전달 
      bomDTO select_bom_by_bom(String bomCode);
      
      
      //bom 삭제 
      int delete_bom_header(String bomCode);
      int delete_bom_detail(String bomCode);
      
      
      
	
}
