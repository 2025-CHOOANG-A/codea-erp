package kr.co.codea.bom;

import java.util.List;
import java.util.Map;

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
	
	//bom 등록시 제품 리스트 불러오기 위한 항목 
	@Override
	public List<bomDTO> bom_item_list_y() {
		 List<bomDTO> item_bomok_List = this.b_mp.bom_item_list_y();
		return item_bomok_List;
	}
	
	
	/*BOM수정 파트*/
	
	@Override
	public bomDTO selectBomHeaderByCode(String bomCode) {
		 return b_mp.selectBomHeaderByCode(bomCode);  
	}
	 
	
	
	//제품 상단조회
	@Override
	public List<bomDTO> selectBomHeaderByBomCode(String bomCode) {
		 List<bomDTO> select_header_list = this.b_mp.selectBomProductByCode();
		return select_header_list;
	}
	
	//자재 하단 조회
	@Override
	public List<bomDTO> selectBomDetailByBomCode(String bomCode) {
		List<bomDTO> select_detail_list = this.b_mp.selectBomProductByCode();
		return select_detail_list;
	}
	
	
	//BOM등록
	//Header(제품 조회 등록)
    @Override
    public int insert_bom_header(bomDTO dto) {
    	return b_mp.insert_bom_header(dto);
    }
	   
    //BOM 상세정보(디테일)
	@Override
    public int insert_bom_detail(bomDTO dto) {
	  return b_mp.insert_bom_detail(dto);
    }
	
	//BOM 전체삭제 
   @Override
    public int delete_bom_details(String bomCode) {
    	return b_mp.delete_bom_details(bomCode);
    }   
  
   //BOM 수정하기위해 BOM 코드 링크 전달 
   @Override
    public bomDTO select_bom_by_bom(String bomCode) {
	   return b_mp.select_bom_by_bom(bomCode);
   }
  
   @Override
   public List<bomDTO> edite_bom_detail(String bomCode) {
	return b_mp.edite_bom_detail(bomCode);
}
   
   
  //BOM 자재 하단 
   
   /*BOM전체삭제(header,detail)*/
 
   @Override
    public int delete_bom_header(String bomCode) {
	    return  b_mp.delete_bom_header(bomCode);
     }
   
   
   @Override
    public int delete_bom_detail(String bomCode) {
	   return b_mp.delete_bom_detail(bomCode);
    }
   
   
   /********************************/
   
   @Override
   public int modify_bom_detail(bomDTO dto) {
       int result = 0;

       // 1. 기존 자재 전체 삭제
       b_mp.delete_bom_details(dto.getBomCode());

       // 2. 새 자재 목록 등록
       if (dto.getMaterials() != null) {
           for (bomDTO material : dto.getMaterials()) {
               if (material.getMaterialCode() != null && !material.getMaterialCode().isBlank()) {
                   material.setBomHeaderId(dto.getBomCode()); // 부모 BOM ID 세팅
                   result += b_mp.insert_bom_detail(material); // 등록된 행 수 누적
               }
           }
       }

       return result; // 총 몇 건 등록됐는지 반환
     }
   
   
   /*페이징 처리*/  
   @Override 
    public List<bomDTO> select_bomList(Map<String, Object> params) {
	return this.b_mp.select_bomList(params);
    }
   
   //page 카운트  
    @Override
   public int select_bomCount(Map<String, Object> params) {
	return this.b_mp.select_bomCount(params);
   }  
   
   
   
   
   
   
   
   
   
   
   
}
