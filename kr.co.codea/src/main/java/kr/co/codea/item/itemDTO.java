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

	//단위 
	private int unitCode;
	private String unitName;     
	private String Code;  
	
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
	    
     private String unitcodeValue;
	
     private String bpTel;
     
     
     
     //조회용
     private String keyword;
     
     
     private String unitcodeText;
     
     // ── Getter / Setter ──

     public String getItemId() {
         return itemId;
     }
     public void setItemId(String itemId) {
         this.itemId = itemId;
     }

     public String getItemCode() {
         return itemCode;
     }
     public void setItemCode(String itemCode) {
         this.itemCode = itemCode;
     }

     public String getItemName() {
         return itemName;
     }
     public void setItemName(String itemName) {
         this.itemName = itemName;
     }

     public String getSpec() {
         return spec;
     }
     public void setSpec(String spec) {
         this.spec = spec;
     }

     public int getPrice() {
         return price;
     }
     public void setPrice(int price) {
         this.price = price;
     }

     public String getReMark() {
         return reMark;
     }
     public void setReMark(String reMark) {
         this.reMark = reMark;
     }

     public String getItemType() {
         return itemType;
     }
     public void setItemType(String itemType) {
         this.itemType = itemType;
     }

     public Date getCreatedAt() {
         return createdAt;
     }
     public void setCreatedAt(Date createdAt) {
         this.createdAt = createdAt;
     }

     public String getInventoryCode() {
         return inventoryCode;
     }
     public void setInventoryCode(String inventoryCode) {
         this.inventoryCode = inventoryCode;
     }

     public int getCurrentQty() {
         return currentQty;
     }
     public void setCurrentQty(int currentQty) {
         this.currentQty = currentQty;
     }

     public String getWhId() {
         return whId;
     }
     public void setWhId(String whId) {
         this.whId = whId;
     }

     public String getWhCode() {
         return whCode;
     }
     public void setWhCode(String whCode) {
         this.whCode = whCode;
     }

     public String getItemCatL() {
         return itemCatL;
     }
     public void setItemCatL(String itemCatL) {
         this.itemCatL = itemCatL;
     }

     public String getItemCatS() {
         return itemCatS;
     }
     public void setItemCatS(String itemCatS) {
         this.itemCatS = itemCatS;
     }

     public String getCategoryId() {
         return categoryId;
     }
     public void setCategoryId(String categoryId) {
         this.categoryId = categoryId;
     }

     public String getCategoryName() {
         return categoryName;
     }
     public void setCategoryName(String categoryName) {
         this.categoryName = categoryName;
     }

     public int getCategoryLevel() {
         return categoryLevel;
     }
     public void setCategoryLevel(int categoryLevel) {
         this.categoryLevel = categoryLevel;
     }

     public String getParentId() {
         return parentId;
     }
     public void setParentId(String parentId) {
         this.parentId = parentId;
     }

     public int getUnitCode() {
         return unitCode;
     }
     public void setUnitCode(int unitCode) {
         this.unitCode = unitCode;
     }

     public String getUnitName() {
         return unitName;
     }
     public void setUnitName(String unitName) {
         this.unitName = unitName;
     }

     public String getUnitcodeValue() {
         return unitcodeValue;
     }
     public void setUnitcodeValue(String unitcodeValue) {
         this.unitcodeValue = unitcodeValue;
     }

     public int getCodeId() {
         return codeId;
     }
     public void setCodeId(int codeId) {
         this.codeId = codeId;
     }

     public String getCodeDesc() {
         return codeDesc;
     }
     public void setCodeDesc(String codeDesc) {
         this.codeDesc = codeDesc;
     }

     public String getCode() {
         return Code;
     }
     public void setCode(String code) {
         this.Code = code;
     }

     public String getBpName() {
         return bpName;
     }
     public void setBpName(String bpName) {
         this.bpName = bpName;
     }

     public String getBpCode() {
         return bpCode;
     }
     public void setBpCode(String bpCode) {
         this.bpCode = bpCode;
     }

     public String getTel() {
         return Tel;
     }
     public void setTel(String tel) {
         this.Tel = tel;
     }

     public int getLeadTime() {
         return leadTime;
     }
     public void setLeadTime(int leadTime) {
         this.leadTime = leadTime;
     }

     public String getKeyword() {
         return keyword;
     }
     public void setKeyword(String keyword) {
         this.keyword = keyword;
     } 
     
     
     
}
