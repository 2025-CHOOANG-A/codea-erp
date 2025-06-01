package kr.co.codea.company;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import jakarta.annotation.Resource;



@Repository("cdn_model")
public class cdn_model {
	
	//FTP정보를 외부에서 수정 못하도록 final 사용
	/*//ftp가 안되는 관계로 sftp사용
	final String user ="erp";
	final String pass = "choongang";
	final int port = 9022;
	final String url = "210.178.108.186";
	FTPClient fc = null;
	FTPClientConfig fcc = null;
	boolean result = false; //FTP 전송 결과 true (업로드 정상), false(오류발생)
	*/
	
    // SFTP 정보
    final String user = "erp";
    final String pass = "choongang";
    final int port = 9022; // SFTP는 기본 22번 포트
    final String url = "210.178.108.186";
	
	@Autowired
	private CompanyService cs;
	
	@Resource(name="CompanyDTO")
	CompanyDTO Company_DTO;
	
	/*
	//CDN Server로 해당 파일을 전송하는 역할을 담당하는 모델
	public boolean cdn_ftp(MultipartFile files)throws Exception {
		try {
			
			/*
			//UUID 사용시
			String ext = originalName.substring(originalName.lastIndexOf("."));
			String uuid = UUID.randomUUID().toString();
			String new_file = uuid + ext; 
			*/
	/*
			String new_file = files.getOriginalFilename();
			this.fc = new FTPClient();
			this.fc.setControlEncoding("utf-8");
			this.fcc = new FTPClientConfig();
			
			this.fc.configure(fcc);
			this.fc.connect(url,this.port); //FTP 연결
			if(this.fc.login(this.user, this.pass)) {
				this.fc.setFileType(FTP.BINARY_FILE_TYPE);
				this.result = this.fc.storeFile("/sxxx/"+new_file, files.getInputStream());
			}
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
    public boolean cdn_ftp(MultipartFile files) {
        Session session = null;
        ChannelSftp channelSftp = null;
        boolean result = false;
        try {
            String new_file = files.getOriginalFilename();

            JSch jsch = new JSch();
            session = jsch.getSession(user, url, port);
            session.setPassword(pass);

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            session.connect();
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            // 실제 파일 업로드
            try (InputStream fis = files.getInputStream()) {
                channelSftp.put(fis, "codea/" + new_file);
                result = true; // 업로드 성공
            }

        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        } finally {
            if (channelSftp != null && channelSftp.isConnected()) channelSftp.disconnect();
            if (session != null && session.isConnected()) session.disconnect();
        }
        return result;
    }
	
}
