package kr.co.codea.item;

import java.util.List;
import java.util.Map;

public interface item_service {
	
	
	/*조회용(주문등록을 하기 위한 선택 항목) 검색조회*/
	    public List<itemDTO> item_select();//주문목록 조회 

	    public List<itemDTO> bp_select();//거래처 조회 
	    
	    public List<itemDTO> item_cg_select();//아이템 카테고리(대분류ㅡ, 소분류 항목)
	    
	    public List<itemDTO> selectCatL(); //대분류만 조회
	    
	    public List<itemDTO> selectCatS(); //소뷴류만 조회
	    
	    public List<itemDTO> unitcode_list();//단위 조회
	    
	    /*제품 상세 정보*/
        public List<itemDTO> item_detail(); //id로 경로 전송해서 띄우기 
	    
         
	   /*주문등록용 */     
	   int insert_item(itemDTO dto);//제품등록
	 
	    /*제품 수정, 삭제*/
	   int update_item(itemDTO dto);
	   int delete_item(String itemId);// 삭제 
	   
	   /*수정, 삭제를 위해 불러옴*/
	   public itemDTO select_item_by_id(String itemId);//상세, 수정 동시에 사용
	  // public itemDTO select_item_by_bp(String bpCode);
	   
	   
	   /*페이징처리*/
	   
	    /** (1) ROW_NUMBER() 페이징 쿼리 */
	   public List<itemDTO> select_item_page(Map<String, Object> params);

	    /** (2) 페이징용 전체 건수 조회 */
	    int page_count(Map<String, Object> params);
	
	   
}
