package kr.co.codea.company;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
		System.out.println(alldata.size());
        return "company/company_detail"; // templates/company/company_detail.html
    }
    @GetMapping("/modify")
    public String CompanyModify() {
        return "company/company_modify"; // templates/company/company_modify.html
    }
}
