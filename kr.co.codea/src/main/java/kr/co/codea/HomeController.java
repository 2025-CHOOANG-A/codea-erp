package kr.co.codea;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import kr.co.codea.auth.dto.UserDetailsDto;
import kr.co.codea.receiving.ReceivingService;
import kr.co.codea.shipment.ShipmentService;

@Controller
public class HomeController {

    private final ShipmentService shipmentService;
    private final ReceivingService receivingService;

    // 생성자 주입
    public HomeController(ShipmentService shipmentService, ReceivingService receivingService) {
        this.shipmentService = shipmentService;
        this.receivingService = receivingService;
    }

    @GetMapping("/index")
    public String indexPage(@AuthenticationPrincipal UserDetailsDto userDetails, Model model) {
        if (userDetails != null) {
            model.addAttribute("userLoginId", userDetails.getUsername());
            model.addAttribute("userRealName", userDetails.getEmpName());
            model.addAttribute("empId", userDetails.getEmpId());
        }

        // 최신 입고/출고 각각 5건 조회
        model.addAttribute("recentReceivingList", receivingService.getRecentReceivingList(5));
        model.addAttribute("recentShipmentList", shipmentService.getRecentShipmentList(5));

        return "index";
    }
}

