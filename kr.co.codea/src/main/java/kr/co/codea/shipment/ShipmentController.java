package kr.co.codea.shipment;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/shipment")
public class ShipmentController {

    @GetMapping("/detail")
    public String detailPage() {
        return "shipment/shipment_detail"; // templates/shipment/shipment_detail.html
    }
   
}


