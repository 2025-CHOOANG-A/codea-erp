package kr.co.codea.mrp;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 자재소요계획(MRP) 메인 컨트롤러
 */
@Controller
@RequestMapping("/mrp")
public class MrpController {

    // MRP 메인 화면 출력
    @GetMapping
    public String showMrpMain() {
        return "mrp/mrp_calc";
    }
}
