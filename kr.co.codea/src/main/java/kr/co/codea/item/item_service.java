package kr.co.codea.item;

import java.util.List;

public interface item_service {
	
	
	/*조회용(주문등록을 하기 위한 선택 항목)*/
	    public List<itemDTO> item_select();//주문목록 조회 

	    public List<itemDTO> bp_select();//거래처 조회 
	    
	    public List<itemDTO> item_cg_select();//아이템 카테고리(대분류ㅡ, 소분류 항목)
	    
	    public List<itemDTO> selectCatL(); //대분류만 조회
	    
	    public List<itemDTO> selectCatS(); //소뷴류만 조회
	    
	    public List<itemDTO> unitcode_list();//단위 조회
	    
	    
	/*주문등록용 */     
	   int insert_item(itemDTO dto);//제품등록
	 
	    
	   
}
