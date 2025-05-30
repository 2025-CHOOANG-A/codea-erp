package kr.co.codea.bom;

import java.util.List;


public interface bom_service {

    // 1. BOM 등록 (헤더 + 디테일)
    //void registerBOM(BomHeaderDTO headerDTO);

    // 2. 완제품 항목 조회
    public List<bomDTO> bom_item_type_y(); // itemType = "완제품"

    // 3. 자재 항목 조회
    public List<bomDTO> bom_item_type_j(); // itemType = "자재"
	
	//BOM 주문목록
    public List<bomDTO> selectBomList(); 
	
}
