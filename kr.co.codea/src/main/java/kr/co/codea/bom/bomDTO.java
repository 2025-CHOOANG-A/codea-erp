package kr.co.codea.bom;

import org.springframework.stereotype.Repository;

import lombok.Data;

@Data
@Repository(" bomDTO")
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
	
	
}
