package kr.co.codea.company;

import java.text.SimpleDateFormat;
import java.util.Date;

//import org.apache.commons.net.ftp.FTP;
//import org.apache.commons.net.ftp.FTPClient;
//import org.apache.commons.net.ftp.FTPClientConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;

@Repository("cdn_model")
public class cdn_model {
	/*
	//FTP정보를 외부에서 수정 못하도록 final 사용
	final String user ="erp";
	final String pass = "choongang";
	final int port = 21;
	final String url = "210.178.108.186";
	FTPClient fc = null;
	FTPClientConfig fcc = null;
	
	boolean result = false; //FTP 전송 결과 true (업로드 정상), false(오류발생)
	
	@Autowired
	private CompanyService cs;
	
	@Resource(name="CompanyDTO")
	CompanyDTO Company_DTO;
	
	//CDN Server로 해당 파일을 전송하는 역할을 담당하는 모델
	public boolean cdn_ftp(MultipartFile files)throws Exception {
		try {
			
			//UUID 사용시
			String ext = originalName.substring(originalName.lastIndexOf("."));
			String uuid = UUID.randomUUID().toString();
			String new_file = uuid + ext; 
			
			String new_file = files.getOriginalFilename();
			this.fc = new FTPClient();
			this.fc.setControlEncoding("utf-8");
			this.fcc = new FTPClientConfig();
			
			this.fc.configure(fcc);
			this.fc.connect(url,this.port); //FTP 연결
			if(this.fc.login(this.user, this.pass)) {
				this.fc.setFileType(FTP.BINARY_FILE_TYPE);
				this.result = this.fc.storeFile("/sxxx/"+new_file, files.getInputStream());
				
				//DB저장
				//this.Company_DTO.setCompImg(files.getOriginalFilename());
				//this.Company_DTO.setBizImg(new_file);
				
				//String api_nm[] = new_file.split("\\.");
				//String api_nm[] = new_file.split("[.]");
				//String api_nm = new_file.substring(0, new_file.lastIndexOf("."));
				//this.Company_DTO.setAPI_FILE(api_nm[0]);
				
				//String api_url = "http://"+url+"/sxxx/"+new_file;
				//this.Company_DTO.setFILE_URL(api_url);
				
				//int result = this.cs.insertCompany(this.Company_DTO);
				
				//Database에 insert 부분에 문제가 발생시 false로 변경하여 Controller로 이관
				// 어짜피 파일이 들어가면 성공한거기 때문에 0일때를 if문으로 박아놈
				//if(result == 0) {
					//this.result = false;
					
				//}
			}
			//System.out.println(new_file);
		}catch(Exception e) {
			System.out.println(e);
		}finally {
		    if (fc != null && fc.isConnected()) {
		        fc.logout();
		        fc.disconnect();
		    }
		}
		
		return this.result;
	}
	*/
}
