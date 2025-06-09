package kr.co.codea.shipment;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.co.codea.inventory.InventoryDTO;
import kr.co.codea.inventory.InventoryMapper;
import kr.co.codea.inventory.InventoryService;

@Service
public class ShipmentDAO implements ShipmentService {
	
	@Autowired
	private ShipmentMapper mp;
	
	@Autowired
	private InventoryMapper inv_mp;
	
	@Autowired
	private InventoryService inv_se;
	
	@Autowired
	private ShipmentMapper shipmentMapper;
	
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
	public Integer ship_inv(int itemId, int whId) {	// 보유 수량
		Integer currentQty = this.mp.ship_inv(itemId, whId);
		
		if(currentQty == null) {
			currentQty = 0;
		}
		
		return currentQty;
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
		
		InventoryDTO before = this.inv_mp.get_inv(dto.getItemId(), dto.getWhId());
		
		if(before != null) {
			int newQty = before.getCurrentQty() - dto.getQuantity();
			
			InventoryDTO update = new InventoryDTO();
			update.setItemId(dto.getItemId());
			update.setWhId(dto.getWhId());
			update.setCurrentQty(newQty);
			update.setAverageCost(before.getAverageCost());
			update.setEmpNo(dto.getEmpNo());
			
			this.inv_mp.inv_qty_update(update);
		}
		
		return result;
	}
	
	@Override
	public List<ShipmentDTO> getRecentShipmentList(int size) {
	    return shipmentMapper.selectRecentShipmentList(size);
	}
}