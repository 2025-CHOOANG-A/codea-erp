package kr.co.codea.inventory;

import org.springframework.stereotype.Repository;

import lombok.Data;

@Data
@Repository("InventoryDTO")
public class InventoryDTO {
	// 자동 증가값, 제품 코드, 창고 코드, 보유 재고, 입고될 수량, 출고될 수량, 평균 단가 -> inventoryId
	int inventoryId, itemId, whId, currentQty, expectedQty, allocatedQty, averageCost;
	
	// 재고 번호, 담당자 사원 번호, 비고, 최초 등록 시각, 최종 업데이트 시각
	String inventoryCode, empNo, remark, createdAt, updatedAt;
	
	// 제품 코드, 제품명, 제품 구분, 규격, 단위, 창고 코드, 창고명, 담당자명
	String itemCode, itemName, itemType, spec, code, whCode, whName, empName;
	
	// 담당자 전화번호
	String hp;
	
	// 창고 주소, 창고 상세주소
	String address, addressDetail;
}