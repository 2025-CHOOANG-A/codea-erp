package kr.co.codea.bom;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;



@Mapper
public interface bom_mapper {

  //bom등록 
 	
  /*완제품조회(ITEM)*/
	//List<bomDTO> bom_item_type_y();
  List<bomDTO> bom_item_type_y();
	
	
	
	 
}
