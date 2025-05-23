package kr.co.codea.item;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class item_service {
	/*
	 결과값을 여러개 사용해야 하므로 result 변수값을ㄹ 사용 하지 않을예정
	 예시)  List<item_list_DTO> result = item_mp.item_select();	 
	 		return result;	
	 */
	
	@Autowired
	item_mapper item_mp; //item mapper 
	
	//주문등록
	public void insert_item(item_list_DTO dto) {
		
		item_mp.insert_item(dto);//주문 등록은 반환값이 필요없음
	}
	
	//주문 전체등록 조회
	
	public List<item_list_DTO> item_select(){
	
		return item_mp.item_select(); 
		
	}

	//주문목록 검색
	public List<item_list_DTO> item_search(String keyword) {
		
		 if (keyword == null || keyword.trim().isEmpty()) {
		        return item_mp.item_select(); // 전체 조회
		    } else {
		        return item_mp.item_search(keyword); // 검색어로 조회
		    }
	}
}