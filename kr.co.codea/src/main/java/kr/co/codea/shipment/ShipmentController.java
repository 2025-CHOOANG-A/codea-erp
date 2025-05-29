package kr.co.codea.shipment;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/shipment")
public class ShipmentController {

	@GetMapping("/list")
	public String listPage() {
		
    	return "shipment/shipment_list"; // templates/shipment/shipment_list.html
	}
	
	@GetMapping("/detail")
	public String detailPage() {
		
    	return "shipment/shipment_detail"; // templates/shipment/shipment_detail.html
	}
	
	@GetMapping("/write")
	public String writePage() {
		
    	return "shipment/shipment_write"; // templates/shipment/shipment_write.html
	}
}
