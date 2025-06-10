package kr.co.codea;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import kr.co.codea.auth.dto.UserDetailsDto;
import kr.co.codea.receiving.ReceivingDTO;
import kr.co.codea.receiving.ReceivingService;
import kr.co.codea.shipment.ShipmentDTO;
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

        // 전체 리스트 가져오기
        List<ReceivingDTO> allReceivingList = receivingService.rec_list(null, null, null);
        List<ShipmentDTO> allShipmentList = shipmentService.ship_list(null, null, null);
        // 최대 5개만 추출
        List<ReceivingDTO> recentReceivingList = allReceivingList.stream().limit(5).toList();
        List<ShipmentDTO> recentShipmentList = allShipmentList.stream().limit(5).toList();

        model.addAttribute("recentReceivingList", recentReceivingList);
        model.addAttribute("recentShipmentList", recentShipmentList);


        return "index";
    }
}

