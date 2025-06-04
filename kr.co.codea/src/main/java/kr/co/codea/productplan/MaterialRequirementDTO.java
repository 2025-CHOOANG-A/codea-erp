package kr.co.codea.productplan;

import lombok.Data;

@Data
public class MaterialRequirementDTO {
	
    private String planId;           // 생산계획 ID
    private int itemId;              // 완제품 ID
    private int materialId; //MRP계산 결과로 가져온 ID
    private String itemCode;         // 자재 품목 코드
    private String itemName;         // 자재 품목명
    private int requiredQty;         // 필요 수량
    private String unit;             // 단위
    private double unitCost;         // 단위 원가
    private int whId;                // 자재가 있는 창고 ID
    private String whName;           // 창고명
    private int availableQty;        // 가용 재고량
    private int empId; //담당id
    private Integer planNo;  
}
