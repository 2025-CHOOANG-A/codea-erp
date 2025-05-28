package kr.co.codea.receiving;

import org.springframework.stereotype.Repository;

import lombok.Data;

@Data
@Repository("ReceivingDTO")
public class ReceivingDTO {
	// 입출고 ID, 거래 유형, 제품 번호, 창고 번호, 수량, 단가
	int inoutId, inoutType, itemId, whId, quantity, itemUnitCost;
	
	// 입출고 시각, 제품 코드, 창고 코드, 비고, 기록 생성 시각, 수정 시각
	String inoutTime, itemCode, whCode, remark, createdAt, updatedAt;
	
	// 원천 문서 유형, 원천 문서 헤더 ID, 원천 문서 상세 ID, 담당자 ID
	int sourceDocType, sourceDocHeaderId, sourceDocDetailId, empId;
	
	// 제품명, 제품 구분, 발주 번호, 발주 일자, 생산 번호, 생산 일자(생산 완료일), 창고명
	String itemName, itemType, purchaseCode, orderDate, planId, completionDate, whName;
	
	// 발주 ID
	int purchaseId;
	
	// 발주 및 생산 번호, 발주 및 생산 일자
	String orderOrPlanNo, orderOrPlanDate;
}