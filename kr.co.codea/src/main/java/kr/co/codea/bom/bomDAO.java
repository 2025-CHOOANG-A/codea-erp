package kr.co.codea.bom;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;




@Service
public class bomDAO implements bom_service{

	@Autowired
	bom_mapper b_mp;
	
	//완제품 조회
	@Override
	public List<bomDTO> bom_item_type_y() {
		List<bomDTO> bom_item_y_list= this.b_mp.bom_item_type_y();
		return bom_item_y_list;
	}
	
	@Override
	public List<bomDTO> bom_item_type_j() {
		List<bomDTO> bom_item_j_list= this.b_mp.bom_item_type_j();
		return bom_item_j_list;
	}
	
	//BOM 등록목록
	@Override
	public List<bomDTO> selectBomList() {
		 List<bomDTO> select_bomList = this.b_mp.selectBomList();
		return select_bomList;
	}
	
}
