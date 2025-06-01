package kr.co.codea.mrp;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import kr.co.codea.company.CompanyService;
import kr.co.codea.productplan.ProductPlanDTO;

/**
 * 자재소요계획(MRP) 메인 컨트롤러
 */
@Controller
@RequestMapping("/mrp")
public class MrpController {
	
	@Autowired
	private MrpService ms; //@Service의 class 로드
	
	
    // MRP 메인 화면 출력
   /* @GetMapping
    public String showMrpMain() {
        return "mrp/mrp_calc";
    }*/
    
	//생산계획 리스트 출력
	@GetMapping
	public String productPlanList(Model m, ProductPlanDTO dto) {
		List<ProductPlanDTO> list = ms.ProductPlanList(dto);
		//Collections.reverse(list); 
        m.addAttribute("productionPlans", list);
		return "mrp/mrp_calc";
	}
	//생산계획에서 계산된 값 ajax로 보내기
	@PostMapping("/calculate")
	@ResponseBody
	public List<MrpDTO> SelectCalcData(@RequestBody List<String> planIds) {
	    return ms.SelectCalcData(planIds);
	}
	
	public int insertMrp(MrpDTO dto) {
		
		return 0;
	}
	
}
