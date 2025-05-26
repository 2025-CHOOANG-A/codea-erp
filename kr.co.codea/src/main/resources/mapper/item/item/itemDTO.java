package kr.co.codea.item;

import java.util.Date;

import org.springframework.stereotype.Repository;

import lombok.Data;

@Data
@Repository("itemDTO")
public class itemDTO {

   //제품목록 리스트 
    private String inventoryCode; // 재고번호
	private String itemCatL; // 대분류
	private String itemCatS; // 소분류
	private String itemCode; // 제품코드(제품목록리스트, 주문등록)
	private String itemName; // 제품명
	private String spec;    // 규격
	private int price;      // 단가
	private int currentQty; // 보유수량
	private String whId;   // 재고창고(WH_ID)
	private String whCode; // 창코코드
	private String reMark; // 비고
	          
    //거래처 정보     
     private String bpName;// 거래처이름
     private String bpCode;// 거래처코드 
     private String Tel;// 거래처 전화번호
	     	    
     //아이템 카테고리(대분류 , 소분류 항목 선택)
     private String categoryId;
     private String categoryName;
     private int categoryLevel;
     private String parentId;
	    
    //단위 항목 불러오기       
     private int codeId;     // 숫자 코드
     private String codeDesc;  // EA, BOX, 등   (별명으로 잡음)
     
     private String itemId;  // 자동증가값 
     private String itemType; // 제품코드 
     private int leadTime;  // LEAD_TIME 품목을 생산하는 데 소요되는 기간을 의미, 각 거래처의 납기기간
     private Date createdAt;  //등록일
	    
		
}
