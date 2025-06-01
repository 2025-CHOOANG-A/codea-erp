package kr.co.codea.shipment;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ShipmentDAO implements ShipmentService {
	
	@Autowired
	private ShipmentMapper mp;
	
	@Override
	public List<ShipmentDTO> ship_list(Integer sourceDocType, String field, String keyword) {	// 목록 페이지
		List<ShipmentDTO> list = this.mp.ship_list(sourceDocType, field, keyword);
		
		return list;
	}
	
	@Override
	public ShipmentDTO ship_detail(int inoutId, int sourceDocType) {	// 상세 페이지
		ShipmentDTO detail = this.mp.ship_detail(inoutId, sourceDocType);
		
		return detail;
	}
	
	@Override
	public List<ShipmentDTO> ship_sea_item(int sourceDocType, String docDate) {	// 등록 페이지 제품 검색
		List<ShipmentDTO> sea_item = this.mp.ship_sea_item(sourceDocType, docDate);
		
		return sea_item;
	}
	
	@Override
	public List<ShipmentDTO> ship_sea_wh(String whName) {	// 등록 페이지 창고 검색
		List<ShipmentDTO> sea_wh = this.mp.ship_sea_wh(whName);
		
		return sea_wh;
	}
	
	@Override
	public List<ShipmentDTO> ship_sea_emp(String empName) {	// 등록 페이지 담당자 검색
		List<ShipmentDTO> sea_emp = this.mp.ship_sea_emp(empName);
		
		return sea_emp;
	}
	
	@Override
	public String check(int sourceDocType, int sourceDocHeaderId, int itemId) {	// 출고 중복 체크
		String msg = "";
		
		Integer ship_ck = this.mp.ship_check(sourceDocType, sourceDocHeaderId, itemId);
		
		if(ship_ck != null && ship_ck > 0) {	// 이미 등록이 되었을 경우
			msg = "exist";
		}
		else {
			msg = "ok";
		}
		
		return msg;
	}
	
	@Override
	public Integer ship_insert(ShipmentDTO dto) {	// 출고 등록
		int result = this.mp.ship_insert(dto);
		
		return result;
	}
}