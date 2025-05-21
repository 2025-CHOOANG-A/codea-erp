package kr.co.codea.item;

import org.springframework.stereotype.Repository;

import lombok.Data;

@Data
@Repository("item_DTO")
public class item_DTO {
	/*
	 ITEM_CODE : 제품코드
	 ITEM_NAME : 제품이름
	 
	 * 
	 */
	
	int ITEM_ID, UNIT_CODE;
	String ITEM_CODE,ITEM_NAME,ITEM_TYPE,ITEM_CATL_L,ITEM_CAT_S;
	
	
}
