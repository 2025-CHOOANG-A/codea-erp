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
		List<itemDTO> item_list = this.mp.item_select();
		
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
	
	
	
	
	
}
