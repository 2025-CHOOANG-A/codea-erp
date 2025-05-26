package kr.co.codea.item;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface item_mapper {
	/*
	//주문등록
	public insert_item(itemDTO dto);
	
	//거래처등록
	public insert_partner(itemDTO dto);

	*/
	
	
/*테이블 조회 : 등록시 필요한 리스트 가져오*/
	
	//주문목록 조회
	List<itemDTO> item_select();	
	//거래처 등록 조회
	List<itemDTO> bp_select();
	
	//대분류, 소분류 항목 조회
	List<itemDTO> item_cg_select();
    //대분류만 조회
	List<itemDTO> selectCatL();

	//소분류만 조회
	List<itemDTO> selectCatS();	
	//단위조회
	List<itemDTO> unitcode_list();
	// 조건 검색용
	//keyword => 검색창 작동
	List<itemDTO> item_search(@Param("keyword") String keyword); 
    
    
}
