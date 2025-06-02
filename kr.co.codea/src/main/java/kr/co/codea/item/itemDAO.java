package kr.co.codea.item;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class itemDAO implements item_service{

	@Autowired
	private item_mapper mp;
	
	
	@Override
	public List<itemDTO> item_select() { //제품목록 페이지  
		List<itemDTO> item_list = this.mp.item_select(); //검색 		
		return item_list;
	}

	@Override
	public List<itemDTO> bp_select() { //거래처 항목 조회 (선택)
	   List<itemDTO> bp_list = this.mp.bp_select();
		return bp_list;
	}
	
	@Override
	public List<itemDTO> item_cg_select() { //대분류 소분류(조회칼럼)
		 List<itemDTO> item_cg_list = this.mp.item_cg_select() ;
         return item_cg_list;
	}
	
	//대분류만조회
	@Override
	public List<itemDTO> selectCatL() {
		 List<itemDTO> calL = this.mp.selectCatL();
		return calL;
	}
	
	//소분류만 조회
	@Override
	public List<itemDTO> selectCatS() {
		 List<itemDTO> calS = this.mp.selectCatS();
		return calS;
	}
	
	//단위 항목 
   @Override
   public List<itemDTO> unitcode_list() {
	List<itemDTO> uni_code = this.mp.unitcode_list();
	return uni_code;		
   }
   
   
   /* 제품등록 항목 (등록은 return 값이 필요없음)*/
   @Override //ITEM 등록
   public int insert_item(itemDTO dto) {
       return mp.insert_item(dto);
   }
   
   @Override
   public List<itemDTO> item_detail() {
	
	List<itemDTO> item_detail_list = this.mp.item_detail();	   	   
	return item_detail_list;
   }
   
   /*제품삭제*/
   @Override
   public int delete_item(String itemCode) {
	return mp.delete_item(itemCode);
}
   
   //제품수정
   @Override
    public int update_item(itemDTO dto) {
	return mp.update_item(dto);
}
   
   //수정시 필요한 번호 불러옴 
   /*
   @Override
    public itemDTO select_item_by_code(String itemCode) {
	   return mp.select_item_by_code(itemCode);
}
   */
   //수정시 필요한 번호 불러옴 
   @Override
   public itemDTO select_item_by_id(String itemId) {
       return this.mp.select_item_by_id(itemId);
   }  


}
