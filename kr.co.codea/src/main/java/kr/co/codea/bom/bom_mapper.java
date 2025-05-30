package kr.co.codea.bom;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;



@Mapper
public interface bom_mapper {

  //bom등록  	
	List<bomDTO> bom_item_type_y(); // 완제품
	List<bomDTO> bom_item_type_j(); // 자재
	
	//BOM 주문목록
	List<bomDTO> selectBomList(); 
}
