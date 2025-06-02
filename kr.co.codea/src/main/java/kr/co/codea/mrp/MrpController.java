package kr.co.codea.mrp;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
	public String productPlanList(
		    @RequestParam(value = "startDate", required = false)
		    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
		    @RequestParam(value = "endDate", required = false)
		    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
			Model m, ProductPlanDTO dto) {
	    dto.setStartDate(startDate);
	    dto.setEndDate(endDate);
	    List<ProductPlanDTO> list = ms.ProductPlanList(dto);
		//Collections.reverse(list); 
	    for (ProductPlanDTO plan : list) {
	        int cnt = ms.countMrpByPlanId(plan.getPlanId());
	        plan.setMrpSaved(cnt > 0);
	    }
	    m.addAttribute("productionPlans", list);
		return "mrp/mrp_calc";
	}
	
	//생산계획에서 계산된 값 ajax로 보내기
	@PostMapping("/calculate")
	@ResponseBody
	public List<MrpDTO> SelectCalcData(@RequestBody List<String> planIds) {
	    return ms.SelectCalcData(planIds);
	}
	
	@PostMapping("/saveAll")
	@ResponseBody
	public Map<String, Object> saveMrps(@RequestBody List<MrpDTO> dtoList) {
	    int successCount = 0;
	    for (MrpDTO dto : dtoList) {
	        // insert와 update를 트랜잭션으로 처리
	        try {
	            successCount += ms.insertMrpAndUpdateStatus(dto);
	        } catch (Exception e) {
	            // 실패한 경우 개별로 에러처리 필요하면 이쪽에서
	            // 예시: 로그 남기거나, 실패 count 등 추가 가능
	        }
	    }
	    Map<String, Object> result = new HashMap<>();
	    result.put("message", successCount > 0 ? "저장 완료!" : "저장 실패");
	    result.put("successCount", successCount);
	    return result;
	}
	
}
