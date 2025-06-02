package kr.co.codea.inventory;

import org.springframework.stereotype.Repository;

import lombok.Data;

@Data
@Repository("InventoryDTO")
public class InventoryDTO {
	// 자동 증가값, 제품 코드, 창고 코드, 보유 재고, 입고될 수량, 출고될 수량
	int inventoryId, itemId, whId, currentQty, expectedQty, allocatedQty;
	
	// 평균 단가
	double averageCost;
	
	// 재고 번호, 담당자 사원 번호, 비고, 최초 등록 시각, 최종 업데이트 시각
	String inventoryCode, empNo, remark, createdAt, updatedAt;
	
	// 제품 코드, 제품명, 제품 구분, 규격, 단위, 창고 코드, 창고명, 담당자명
	String itemCode, itemName, itemType, spec, code, whCode, whName, empName;
	
	// 담당자 전화번호
	String hp;
	
	// 창고 주소, 창고 상세주소
	String address, addressDetail;
	
	// 수량
	int quantity;
	
	// 입출고 단가
	double itemUnitCost;
	
	// 로그 ID, 변경 전 보유 수량, 변경 후 보유 수량
	int logId, beforeQty, afterQty;
	
	// 변경 전 평균 단가, 변경 후 평균 단가
	double beforeCost, afterCost;
	
	// 로그 타입, 변경 사유, 변경 일자
	String logType, reason, editDate;
	
	// 제품 단가
	int price;
	
	// 입출고 ID, 거래 유형
	int inoutId, inoutType;
	
	// 입출고 시각
	String inoutTime;
	
	// 총액
	double totalCost;

	// 원천 문서 유형, 원천 문서 헤더 ID
	int sourceDocType, sourceDocHeaderId;
	
	// 발주 번호, 발주 일자, 생산 번호, 생산 일자(생산 완료일), 주문 번호, 주문 일자
	String purchaseCode, purchaseDate, planId, completionDate, ordCode, orderDate;
	
	// 발주 ID, 주문 Id
	int purchaseId, ordId;
	
	// 문서 번호, 문서 일자
	String docNo, docDate;
}