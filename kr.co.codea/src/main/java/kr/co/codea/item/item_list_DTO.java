package kr.co.codea.item;

import org.springframework.stereotype.Repository;

import lombok.Data;

@Data
@Repository("item_list_DTO")
public class item_list_DTO {


        private String inventoryCode; // 재고번호
	    private String itemCatL;      // 대분류
	    private String itemCatS;      // 소분류
	    private String itemCode;      // 제품코드
	    private String itemName;      // 제품명
	    private String spec;          // 규격
	    private int price;         // 단가
	    private int currentQty;       // 보유수량
	    private String whId;   // 재고창고(WH_ID)
	    private String whCode;// 창코코드
	    private String reMark;        // 비고
		
}
