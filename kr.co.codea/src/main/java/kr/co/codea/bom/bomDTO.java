package kr.co.codea.bom;

import java.util.List;

import org.springframework.stereotype.Repository;

import lombok.Data;

@Data
@Repository("bomDTO")
public class bomDTO {	
	/*완제품조회 */
    private String itemId;  // 자동증가값 
    private String itemCode; // 제품코드(제품목록리스트, 주문등록)
	private String itemName; // 제품명
    private String itemType; // 제품코드 
    private String itemCatL; // 대분류
	private String itemCatS; // 소분류
	private String unitCode;
	private String unitName; // ← 조인으로 붙는 필드
	private String spec;    // 규격
	private String reMark; // 비고
	
	
	//ㅠ
	private String bomCode;         // BOM_HEADER_ID
    private String productCode;     // 완제품 코드
    private String productName;     // 완제품 명
    private String materialCode;    // 자재 코드
    private String materialName;    // 자재 명
   // private String spec;            // 규격
   // private String unitName;        // 단위 설명
    private String unitName2;
    private String unitUnicode;      //단위
    private int price;              // 단가
    private int quantity;           // 자재 소요량
    
    
    /* === BOM_HEADER 등록용 === */
    private String bomHeaderId;    // BOM_HEADER_ID (등록용 PK)
    private String version;        // 버전
    private String description;    // 비고
    private String createdAt;      // 생성일시 (조회용)

    /* === BOM_DETAIL 등록용 === */
    private String childId;        // 자재 ITEM_ID (CHILD_ID)          // 자재 단가
    private double lossRate;       // 손실률
    
	 private List<bomDTO> materials;
}
