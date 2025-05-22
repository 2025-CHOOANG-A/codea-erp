package kr.co.codea.item;

import org.springframework.stereotype.Repository;

import lombok.Data;

@Data
@Repository("item_list_DTO")
public class item_list_DTO {
	/*
	칼럼	설명	원 테이블
     INVENTORY_CODE	재고 번호	INVENTORY
     ITEM_CODE	제품 코드	ITEM
     ITEM_NAME	제품 이름	ITEM
     CATEGORY_NAME (대분류)	ITEM_CAT_L → CATEGORY_ID	ITEM_CATEGORY (대분류)
     CATEGORY_NAME (소분류)	ITEM_CAT_S → CATEGORY_ID	ITEM_CATEGORY (소분류)
     WH_NAME	창고명	WAREHOUSE
     CURRENT_QTY	현재 수량	INVENTORY
        AVERAGE_COST	평균단가	INVENTORY
EMP_NO	담당자	INVENTORY
REMARK	비고	INVENTORY


	 
	 * 
	 */

	/*
	private int itemId;
	private int unitCode;
	private String itemCode;
	private String itemName;
	private String itemType;
	private String itemCatL;
	private String itemCatS;	
	*/ 
	    private String inventoryCode; // 재고번호
	    private String itemCatL;      // 대분류
	    private String itemCatS;      // 소분류
	    private String itemCode;      // 제품코드
	    private String itemName;      // 제품명
	    private String spec;          // 규격
	    private int price;         // 단가
	    private int currentQty;       // 보유수량
	    private String whId;   // 재고창고(WH_ID)
	    private String whcd;
	    private String reMark;        // 비고
	
	
	
}
