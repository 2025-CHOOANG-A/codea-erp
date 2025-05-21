package kr.co.codea.company;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/company")
public class CompanyController {

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
    	    @RequestParam("compImg") MultipartFile compImg,
    	    @RequestParam("bizImg") MultipartFile bizImg) {
    	try {
    		
    		if (!compImg.isEmpty()) {
    			String originalFileName = compImg.getOriginalFilename(); // 파일 이름만
    			dto.setCompImg(originalFileName); // 그냥 파일명만 저장 (경로 필요)
    		}
    		if (!bizImg.isEmpty()) {
    			String originalFileName = bizImg.getOriginalFilename(); // 파일 이름만
    			dto.setBizImg(originalFileName); // 그냥 파일명만 저장 (경로 필요)
    		}
    		if (dto.getCompId() == null) {
    			int result = this.cs.insertCompany(dto); // INSERT        		
    			if(result>0) {
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
    	}
    	catch(Exception e) {
    		System.out.println(e);
    	}
        return "company/company_detail"; // templates/company/company_modify.html
    }
    /*
    @PostMapping("/modifydo")
    public String CompanyModifydo(@ModelAttribute CompanyDTO dto,
    	    @RequestParam("compImg") MultipartFile compImg,
    	    @RequestParam("bizImg") MultipartFile bizImg) {
        if (!compImg.isEmpty()) {
            String filePath = saveFile(compImg); // 파일 저장 후 경로 반환
            dto.setCompImg(filePath);
        }
        if (!bizImg.isEmpty()) {
            String filePath = saveFile(bizImg);
            dto.setBizImg(filePath);
        }
        if (dto.getCompId() == null) {
        	int result = this.cs.insertCompany(dto); // INSERT        		
        	if(result>0) {
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
        return "company/company_detail"; // templates/company/company_modify.html
    }*/
}
