package kr.co.codea.item;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface item_mapper {
	
	//주문목록 등록
	public void insert_item(item_list_DTO dto);

	//주문목록 조회
	List<item_list_DTO> item_select();
	
	// 조건 검색용
	//keyword => 검색창 작동
	List<item_list_DTO> item_search(@Param("keyword") String keyword); 
    
    
}
