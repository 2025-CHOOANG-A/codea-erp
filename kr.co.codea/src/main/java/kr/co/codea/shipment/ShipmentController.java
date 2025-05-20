package kr.co.codea.shipment;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/shipment")
public class ShipmentController {

	@GetMapping("/detail")
	public String detailPage() {	// 출고 상세 페이지
    	return "shipment/shipment_detail"; // templates/shipment/shipment_detail.html
	}

	@GetMapping("/write")
	public String writePage() {	// 출고 등록 페이지
    	return "shipment/shipment_write"; // templates/shipment/shipment_write.html
	}
    
	@GetMapping("/list")
	public String listPage() {	// 출고 목록 페이지
    	return "shipment/shipment_list"; // templates/shipment/shipment_list.html
	}
}