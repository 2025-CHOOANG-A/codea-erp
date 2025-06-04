package kr.co.codea.receiving;

import java.sql.Date;

import org.springframework.stereotype.Repository;

import lombok.Data;

@Data
@Repository("ReceivingDTO")
public class ReceivingDTO {
	// 입출고 ID, 거래 유형, 제품 번호, 창고 번호, 수량
	int inoutId, inoutType, itemId, whId, quantity;
	
	// 입출고 시각, 제품 코드, 창고 코드, 비고, 기록 생성 시각, 수정 시각
	String inoutTime, itemCode, whCode, remark, createdAt, updatedAt;
	
	// 입출고 테이블 단가
	double itemUnitCost;
	
	// 원천 문서 유형, 원천 문서 헤더 ID, 원천 문서 상세 ID, 담당자 ID
	int sourceDocType, sourceDocHeaderId, sourceDocDetailId, empId;
	
	// 제품명, 제품 구분, 발주 번호, 발주 일자, 생산 번호, 생산 일자(생산 완료일), 창고명
	String itemName, itemType, purchaseCode, purchaseDate, planId, completionDate, whName;
	
	// 발주 ID, 발주 상세 ID
	int purchaseId, purchaseDetailId;
	
	// 발주 및 생산 번호, 발주 및 생산 일자
	String docNo, docDate;
	
	// 규격, 단위, 발주 금액, 담당자 코드, 담당자명, 담당자 전화번호
	String spec, code, empNo, empName, Hp;
	
	// 제품 단가, 발주 수량, 생산 지시 수량, 발주 및 생산 수량
	int price, orderQty, actualQty, docQty;
	
	// 입고 총액, 발주 및 생산 금액
	double inCost, docCost;
	
	// 가입고 수량
	int qty;
	
	// 창고 주소, 창고 상세 주소
	String address, addressDetail;
}