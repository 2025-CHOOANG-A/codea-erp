package kr.co.codea.bom;

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
    private int price;              // 단가
    private int quantity;           // 자재 소요량
	
}
