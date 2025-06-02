package kr.co.codea.inventory;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InventoryDAO implements InventoryService {

	@Autowired
	private InventoryMapper mp;
	
	@Override
	public List<InventoryDTO> inv_list(String itemType, String field, String keyword) {	// 목록 페이지
		List<InventoryDTO> list = this.mp.inv_list(itemType, field, keyword);
		
		return list;
	}
	
	@Override
	public InventoryDTO inv_detail(int inventoryId) {	// 상세 페이지
		InventoryDTO detail = this.mp.inv_detail(inventoryId);
		
		return detail;
	}
	
	@Override
	public List<InventoryDTO> inout(int itemId, int whId) {	// 입출고 내역
		List<InventoryDTO> inout = this.mp.inout(itemId, whId);
		
		return inout;
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
	public InventoryDTO avg_cost(int currentQty, double averageCost, int itemId, int whId, String itemType) {	// 평균 단가 (기존 수량, 기존 평균 단가, 입고 수량, 입고 단가)
		InventoryDTO in_data_dto = this.mp.in_data(itemId, whId, itemType);
		
		int quantity = in_data_dto.getQuantity();	// 입고 수량
		double itemUnitCost = in_data_dto.getItemUnitCost();	// 입고 단가
		
		int total_qty = currentQty + quantity;

		InventoryDTO dto = new InventoryDTO();

		if(total_qty == 0) {
			dto.setAverageCost(0.0);
		}
		else {
			double new_cost = ((currentQty * averageCost) + (quantity * itemUnitCost)) / total_qty;
			
			dto.setAverageCost(new_cost);
		}
		
		dto.setQuantity(quantity);
		dto.setItemUnitCost(itemUnitCost);
		
		return dto;
	}
	
	@Override
	public String check(int itemId, int whId) {	// 중복 체크
		String msg = "";
		
		Integer inv_ck = this.mp.inv_check(itemId, whId);	// 재고
		Integer io_ck = this.mp.inout_check(itemId, whId);	// 입출고
		
		if(inv_ck != null && inv_ck > 0) {	// 재고 테이블에 데이터가 있을 경우
			msg = "exist";
		}
		else if(io_ck == null || io_ck == 0) {	// 입출고 테이블에 데이터가 없을 경우
			msg = "nodata";
		}
		else {	// 등록 가능
			msg = "ok";
		}
		
		return msg;
	}

	@Override
	public Integer inv_insert(InventoryDTO dto) {	// 재고 등록
		InventoryDTO inv_avg_cost = this.mp.inv_avg_cost(dto.getItemId(), dto.getWhId());
		
		int cur_qty = 0;	// 기존 수량
		double avg_cost = 0;	// 기존 평균 단가
		
		if(inv_avg_cost != null) {	// 기존 재고가 있으면
			cur_qty = inv_avg_cost.getCurrentQty();
			avg_cost = inv_avg_cost.getAverageCost();
		}
		
		InventoryDTO qty_dto = this.inv_dto(dto.getItemId(), dto.getWhId());
		dto.setExpectedQty(qty_dto.getExpectedQty());	// 입고 예정 수량
		dto.setAllocatedQty(qty_dto.getAllocatedQty());	// 출고 예정 수량
		
		InventoryDTO cost_dto = this.avg_cost(cur_qty, avg_cost, dto.getItemId(), dto.getWhId(), dto.getItemType());
		dto.setQuantity(cost_dto.getQuantity());	// 입고 수량
		dto.setItemUnitCost(cost_dto.getItemUnitCost());	// 입고 단가
		dto.setAverageCost(cost_dto.getAverageCost());	// 평균 단가
		
		int result = this.mp.inv_insert(dto);
		
		Integer inventoryId = this.mp.inv_id(dto.getItemId(), dto.getWhId());
		dto.setInventoryId(inventoryId);
		
		dto.setLogType("등록");	// 로그 - 등록 및 수정
		dto.setBeforeQty(cur_qty);	// 로그 - 변경 전 수량 
		dto.setAfterQty(dto.getQuantity());	// 로그 - 변경 후 수량
		dto.setBeforeCost(avg_cost);	// 로그 - 변경 전 평균 단가
		dto.setAfterCost(dto.getAverageCost());	// 로그 - 변경 후 평균 단가
		
		this.mp.inv_log_insert(dto);	// 로그 테이블에 저장
		
		return result;
	}
	
	@Override
	public InventoryDTO inv_mod(int inventoryId) {	// 수정 페이지
		InventoryDTO mod = this.mp.inv_mod(inventoryId);
		
		return mod;
	}
	
	@Override
	public InventoryDTO inv_before(int itemId, int whId) {	// 기존 재고 및 평균 단가 조회
		InventoryDTO before = this.mp.inv_before(itemId, whId);
		
		InventoryDTO dto = new InventoryDTO();
		dto.setCurrentQty(before.getCurrentQty());
		dto.setAverageCost(before.getAverageCost());
		
		return dto;
	}
	
	@Override
	public Integer inv_update(InventoryDTO dto) {	// 재고 수정
		InventoryDTO before = this.mp.inv_before(dto.getItemId(), dto.getWhId());
		
		int beforeQty = before.getCurrentQty();
		double beforeCost = before.getAverageCost();

		InventoryDTO qty_dto = this.inv_dto(dto.getItemId(), dto.getWhId());
		dto.setExpectedQty(qty_dto.getExpectedQty());	// 입고 예정 수량
		dto.setAllocatedQty(qty_dto.getAllocatedQty());	// 출고 예정 수량
		
		InventoryDTO cost_dto = this.avg_cost(beforeQty, beforeCost, dto.getItemId(), dto.getWhId(), dto.getItemType());
		dto.setAverageCost(cost_dto.getAverageCost());	// 평균 단가
		
		int result = this.mp.inv_update(dto);
		
		dto.setLogType("수정");	// 로그 - 등록 및 수정
		dto.setBeforeQty(beforeQty);	// 로그 - 변경 전 수량 
		dto.setAfterQty(dto.getCurrentQty());	// 로그 - 변경 후 수량
		dto.setBeforeCost(beforeCost);	// 로그 - 변경 전 평균 단가
		dto.setAfterCost(dto.getAverageCost());	// 로그 - 변경 후 평균 단가
		
		this.mp.inv_log_update(dto);
		
		return result;
	}
}