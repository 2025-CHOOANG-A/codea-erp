package kr.co.codea.company;

import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
@Repository("CompanyDTO")
public class CompanyDTO {
	Integer compId, bizCond;
	String compName, compType, compImg, ceoName, bizNo;
	String corpNo, bizImg, bizType, address, addressDetail, postCode;
	String tel, fax, mail;
	
	

	MultipartFile compImgFile, bizImgFile;
}
