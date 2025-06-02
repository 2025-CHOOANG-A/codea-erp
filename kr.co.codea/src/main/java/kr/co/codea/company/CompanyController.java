package kr.co.codea.company;


import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import org.springframework.http.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import jakarta.annotation.Resource;


@Controller
@RequestMapping("/company")
public class CompanyController {
	Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Resource(name="cdn_model")
	cdn_model cdn_model;
	
	@Autowired
	private CompanyService cs; //@Service의 class 로드
	
	/*
	 * @Resource(name="CompanyService") CompanyService cs;
	 */
    @GetMapping("/detail")
    public String CompanyDetail(Model m) {

		List<CompanyDTO> alldata=this.cs.alldata();
		m.addAttribute("alldata",alldata);
		System.out.println(alldata.toString());
        return "company/company_detail"; // templates/company/company_detail.html
    }
    
    @GetMapping("/modify")
    public String CompanyModify(Model m) {
		CompanyDTO dto=this.cs.alldata().get(0);
		m.addAttribute("dto",dto);
		System.out.println(dto.toString());
        return "company/company_modify"; // templates/company/company_modify.html
    }
    
    @PostMapping("/modifydo")
    public String CompanyModifydo(@ModelAttribute CompanyDTO dto,
    	    @RequestParam("compImgFile") MultipartFile compImgFile,
    	    @RequestParam("bizImgFile") MultipartFile bizImgFile,
    	    @RequestParam("originCompImg") String originCompImg,
    	    @RequestParam("originBizImg") String originBizImg) {
    	try {
    		
            if (dto.getCompImgFile() != null && !dto.getCompImgFile().isEmpty()) {
                String originalFileName = dto.getCompImgFile().getOriginalFilename();
                dto.setCompImg(originalFileName);
            }
            if (dto.getBizImgFile() != null && !dto.getBizImgFile().isEmpty()) {
                String originalFileName = dto.getBizImgFile().getOriginalFilename();
                dto.setBizImg(originalFileName);
            }
            
            // 회사 로고 이미지 처리
            if (compImgFile != null && !compImgFile.isEmpty()) {
                String fileName = compImgFile.getOriginalFilename();
                dto.setCompImg(fileName);
                cdn_model.cdn_ftp(compImgFile); //FTP실행시 풀면됨
            } else {
                dto.setCompImg(originCompImg); // 기존 파일 유지
            }

            // 사업자등록증 이미지 처리
            if (bizImgFile != null && !bizImgFile.isEmpty()) {
                String fileName = bizImgFile.getOriginalFilename();
                dto.setBizImg(fileName);
                cdn_model.cdn_ftp(bizImgFile);
            } else {
                dto.setBizImg(originBizImg); // 기존 파일 유지
            }
            
    		if (dto.getCompId() == null || dto.getCompId() == 0) {
    			int result = this.cs.insertCompany(dto); // INSERT        		
    			if(result > 0) {
    				System.out.println("insert 성공");
    			}else {
    				System.out.println("insert 실패");
    			}
    		} else {
    			int result = this.cs.updateCompany(dto); // UPDATE        		
    			if(result>0) {
    				System.out.println("update 성공");
    			}
    			else {
    				System.out.println("update 실패");
    			}
    		}
    		
    		try {
    			boolean Fileresult1 = this.cdn_model.cdn_ftp(compImgFile);
    			boolean Fileresult2 = this.cdn_model.cdn_ftp(bizImgFile);
    			if(Fileresult1 == true && Fileresult2 == true) {
    				this.log.info("저장완료");
    			}
    			else {
    				this.log.info("저장실패");
    			}
    		}catch(Exception e) {
    			this.log.info(e.toString());
    		}finally {
    			
    		}
    		
    	}
    	catch(Exception e) {
    		System.out.println(e);
    	}
        return "redirect:/company/detail"; // templates/company/company_modify.html
    }
    
    @GetMapping("/cdnimg/{filename:.+}")
    @ResponseBody
    public ResponseEntity<byte[]> getCdnImage(@PathVariable("filename") String filename) {
        System.out.println("CDNIMG 요청: " + filename);
        byte[] imageBytes = null;
        Session session = null;
        ChannelSftp channelSftp = null;
        try {
            JSch jsch = new JSch();
            session = jsch.getSession("erp", "210.178.108.186", 9022);
            session.setPassword("choongang");
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            session.connect();
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            try (InputStream is = channelSftp.get("codea/" + filename)) {
                imageBytes = is.readAllBytes();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (channelSftp != null && channelSftp.isConnected()) channelSftp.disconnect();
            if (session != null && session.isConnected()) session.disconnect();
        }

        if (imageBytes == null) return ResponseEntity.notFound().build();

        HttpHeaders headers = new HttpHeaders();
        // 확장자별로 Content-Type 자동 설정
        String contentType = "image/png";
        String fname = filename.toLowerCase();
        if (fname.endsWith(".jpg") || fname.endsWith(".jpeg")) {
            contentType = "image/jpeg";
        } else if (fname.endsWith(".gif")) {
            contentType = "image/gif";
        } else if (fname.endsWith(".bmp")) {
            contentType = "image/bmp";
        }
        headers.setContentType(MediaType.parseMediaType(contentType));
        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }

}