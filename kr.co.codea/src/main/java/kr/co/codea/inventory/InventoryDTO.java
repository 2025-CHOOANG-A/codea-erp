package kr.co.codea.inventory;

import org.springframework.stereotype.Repository;

import lombok.Data;

@Data
@Repository("InventoryDTO")
public class InventoryDTO {
	// 자동 증가값, 재고 번호, 제품 코드, 창고 코드, 보유 재고, 입고될 수량, 출고될 수량, 평균 단가 -> inventoryId
	int INVENTORY_ID, INVENTORY_CODE, ITEM_ID, WH_ID, CURRENT_QTY, EXPECTED_QTY, ALLOCATED_QTY, AVERAGE_COST;
	
	// 사원 번호, 비고, 최초 등록 시각, 최종 업데이트 시각
	String EMP_NO, REMARK, CREATE_AT, UPDATED_AT;
}