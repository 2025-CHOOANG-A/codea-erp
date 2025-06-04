package kr.co.codea.receiving;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.co.codea.inventory.InventoryDTO;
import kr.co.codea.inventory.InventoryMapper;
import kr.co.codea.inventory.InventoryService;

@Service
public class ReceivingDAO implements ReceivingService {
	
	@Autowired
	private ReceivingMapper mp;
	
	@Autowired
	private InventoryMapper inv_mp;
	
	@Autowired
	private InventoryService inv_se;
	
	@Override
	public List<ReceivingDTO> rec_list(Integer sourceDocType, String field, String keyword) {	// 목록 페이지
		List<ReceivingDTO> list = this.mp.rec_list(sourceDocType, field, keyword);
		
		return list;
	}
	
	@Override
	public ReceivingDTO rec_detail(int inoutId, int sourceDocType) {	// 상세 페이지
		ReceivingDTO detail = this.mp.rec_detail(inoutId, sourceDocType);
		
		return detail;
	}
	
	@Override
	public List<ReceivingDTO> rec_sea_item(int sourceDocType, String docDate) {	// 등록 페이지 제품 검색
		List<ReceivingDTO> sea_item = this.mp.rec_sea_item(sourceDocType, docDate);
		
		return sea_item;
	}
	
	@Override
	public List<ReceivingDTO> rec_sea_wh(String whName) {	// 등록 페이지 창고 검색
		List<ReceivingDTO> sea_wh = this.mp.rec_sea_wh(whName);
		
		return sea_wh;
	}
	
	@Override
	public List<ReceivingDTO> rec_sea_emp(String empName) {	// 등록 페이지 담당자 검색
		List<ReceivingDTO> sea_emp = this.mp.rec_sea_emp(empName);
		
		return sea_emp;
	}
	
	@Override
	public String check(int sourceDocType, int sourceDocHeaderId, int itemId) {	// 입고 중복 체크
		String msg = "";
		
		Integer rec_ck = this.mp.rec_check(sourceDocType, sourceDocHeaderId, itemId);
		
		if(rec_ck != null && rec_ck > 0) {	// 이미 등록이 되었을 경우
			msg = "exist";
		}
		else {
			msg = "ok";
		}
		
		return msg;
	}
	
	@Override
	public Integer rec_insert(ReceivingDTO dto) {	// 입고 등록
		int result = this.mp.rec_insert(dto);

		InventoryDTO before = this.inv_mp.get_inv(dto.getItemId(), dto.getWhId());
		
		if(before != null) {
			InventoryDTO avg = this.inv_se.avg_cost(before.getCurrentQty(), before.getAverageCost(), dto.getItemId(), dto.getWhId(), dto.getItemType());
			
			int newQty = before.getCurrentQty() + dto.getQuantity();
			
			InventoryDTO update = new InventoryDTO();
			update.setItemId(dto.getItemId());
			update.setWhId(dto.getWhId());
			update.setCurrentQty(newQty);
			update.setAverageCost(avg.getAverageCost());
			update.setEmpNo(dto.getEmpNo());
			
			this.inv_mp.inv_qty_update(update);
		}
		
		return result;
	}
}