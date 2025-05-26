package kr.co.codea.storage;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class StorageDTO {
	//테이블
	private Integer whId;
	private String whCode;
	private String whName;
	private String address;
	private String tel;
	private String addressDetail;
	private String postCode;
	private String remark;
	private String empNo;
	
	//조
	private String empName;
	private String empTel;


	
	//검색관련
	private String keyword;
	private String searchType;
}