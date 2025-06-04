package kr.co.codea.item;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface item_mapper {

	/*주문 등록*/	
    // ITEM 테이블 추가 
    int insert_item(itemDTO dto);
	
    /*테이블 조회 : 등록시 필요한 리스트 가져오*/	
	//주문목록 조회 (제품구분, 검색 전용 포함)
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
	
	

	//item_detail 상세조회 경로 전송 (ITEM_ID)
	List<itemDTO> item_detail();
	
    //item_수정 , 삭제	
    int update_item(itemDTO dto);
    int delete_item(String itemId);
	
    //수정을 하기위해 item 코드불러오기 
    itemDTO select_item_by_id(String itemId); //상세 수정, 동시에 사용함
   // itemDTO select_item_by_bp(String bpCode);
    
	//제품삭제
 
     
    
    
    
    /*페이징처리*/
   
    /** (1) ROW_NUMBER() 페이징 쿼리 */
    List<itemDTO> select_item_page(Map<String, Object> params);

    /** (2) 페이징용 전체 건수 조회 */
    int page_count(Map<String, Object> params);
    
	
	// 조건 검색용
	//keyword => 검색창 작동
    /*
	List<itemDTO> item_search(@Param("keyword") String keyword); 
   */

	
	
}
