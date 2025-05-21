package kr.co.codea.company;

import lombok.Data;

@Data
public class CompanyDTO {
	Integer compId, bizCond;
	String compName, compType, compImg, ceoName, bizNo;
	String corpNo, bizImg, bizType, address, addressDetail, postCode;
	String tel, fax, mail;
}
