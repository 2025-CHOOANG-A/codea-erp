package kr.co.codea.productplan;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/productplan")
public class ProductPlanController {
	
	private ProductPlanService service;
	
	public ProductPlanController(ProductPlanService service) {
		this.service = service;
	}
	
	
	
	//생산계획 리스트
	@GetMapping
	public String productPlanList(Model m, ProductPlanDTO dto) {
		List<ProductPlanDTO> list = service.ProductPlanList(dto);
        m.addAttribute("productionPlans", list);
        m.addAttribute("searchDto", dto); 
		
        m.addAttribute("templateName", "productplan/productplan_list");
        m.addAttribute("fragmentName", "contentFragment");
        
		return "productplan/productplan_default";
	}
	
}
