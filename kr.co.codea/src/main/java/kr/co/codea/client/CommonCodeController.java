package kr.co.codea.client;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/common-codes")
public class CommonCodeController {
    private final ClientService cs;

    public CommonCodeController(ClientService cs) {
        this.cs = cs;
    }

    @GetMapping("/biz-cond")
    public ResponseEntity<List<CommonCodeDTO>> getBizConds(@RequestParam("query") String query) {
        List<CommonCodeDTO> bizConds = cs.findCommonCode("BIZ_COND", query);
        return ResponseEntity.ok(bizConds);
    }
}
