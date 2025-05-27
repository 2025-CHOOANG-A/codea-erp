package kr.co.codea.inventory;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InventoryDAO implements InventoryService {

	@Autowired
	private InventoryMapper mp;
	
	@Override
	public List<InventoryDTO> inv_list(String itemType, String field, String keyword) {
		List<InventoryDTO> list = this.mp.inv_list(itemType, field, keyword);
		
		return list;
	}
	
	@Override
	public InventoryDTO inv_detail(int inventoryId) {	// 상세 페이지
		InventoryDTO detail = this.mp.inv_detail(inventoryId);
		
		return detail;
	}

	@Override
	public List<InventoryDTO> inv_sea_item(String itemName) {	// 등록 및 수정 페이지 제품 검색
		List<InventoryDTO> sea_item = this.mp.inv_sea_item(itemName);
		
		return sea_item;
	}
	
	@Override
	public List<InventoryDTO> inv_sea_wh(String whName) {	// 등록 및 수정 페이지 창고 검색
		List<InventoryDTO> sea_wh = this.mp.inv_sea_wh(whName);
		
		return sea_wh;
	}
	
	@Override
	public List<InventoryDTO> inv_sea_emp(String empName) {	// 등록 및 수정 페이지 담당자 검색
		List<InventoryDTO> sea_emp = this.mp.inv_sea_emp(empName);
		
		return sea_emp;
	}
	
	@Override
	public Integer inv_qty(int itemId, int whId, String itemType) {	// 보유 수량
		int currentQty = 0;
		
		if("원자재".equals(itemType)) {
			currentQty = this.mp.in_qty_pur(itemId, whId) - this.mp.out_qty_pur(itemId, whId);
		}
		else if("완제품".equals(itemType)) {
			currentQty = this.mp.in_qty_pro(itemId, whId) - this.mp.out_qty_pro(itemId, whId);
		}
		
		return currentQty;
	}
	
	@Override
	public InventoryDTO inv_dto(int itemId, int whId) {	// dto 관련 메소드 (입고 예정 수량, 출고 예정 수량)
		Integer expectedQty = this.mp.inv_in_qty(itemId, whId);	// 가입고 수량
		Integer allocatedQty = this.mp.inv_out_qty(itemId, whId);	// 가출고 수량
		
		if(expectedQty == null) {	// 가입고 수량이 없을 때
			expectedQty = 0;
		}
		
		if(allocatedQty == null) {	// 가출고 수량이 없을 때
			allocatedQty = 0;
		}
		
		InventoryDTO dto = new InventoryDTO();
		dto.setExpectedQty(expectedQty);
		dto.setAllocatedQty(allocatedQty);
		
		return dto;
	}
	
	@Override
	public InventoryDTO avg_cost(int currentQty, double averageCost, int quantity, double itemUnitCost) {	// 평균 단가 (기존 수량, 기존 평균 단가, 입고 수량, 입고 단가)
		InventoryDTO dto = new InventoryDTO();

		int total_qty = currentQty + quantity;
		
		if(total_qty == 0) {
			dto.setAverageCost(0.0);
		}
		else {
			double new_cost = ((currentQty * averageCost) + (quantity * itemUnitCost)) / (currentQty + quantity);
			
			dto.setAverageCost(new_cost);
		}
		
		return dto;
	}
	
	@Override
	public Integer inv_insert(InventoryDTO dto) {	// 재고 등록
		InventoryDTO inv_avg_cost = this.mp.inv_avg_cost(dto.getItemId(), dto.getWhId());
		
		int cur_qty = 0;	// 기존 수량
		double avg_cost = 0;	// 기존 평균 단위
		
		if(inv_avg_cost != null) {	// 기존 재고가 있으면
			cur_qty = inv_avg_cost.getCurrentQty();
			avg_cost = inv_avg_cost.getAverageCost();
		}
		
		InventoryDTO in_data = this.mp.in_data(dto.getItemId(), dto.getWhId(), dto.getItemType());
		
		
		InventoryDTO qty_dto = this.inv_dto(dto.getItemId(), dto.getWhId());
		dto.setExpectedQty(qty_dto.getExpectedQty());	// 입고 예정 수량
		dto.setAllocatedQty(qty_dto.getAllocatedQty());	// 출고 예정 수량
		
		InventoryDTO cost_dto = this.avg_cost(cur_qty, avg_cost, dto.getQuantity(), dto.getItemUnitCost());
		dto.setAverageCost(cost_dto.getAverageCost());	// 평균 단가
		
		int result = this.mp.inv_insert(dto);
		
		return result;
	}
}