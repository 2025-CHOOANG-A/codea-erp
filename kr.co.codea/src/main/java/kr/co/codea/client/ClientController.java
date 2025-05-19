package kr.co.codea.client;

import org.springframework.stereotype.Controller; // @Controller 사용
import org.springframework.ui.Model; // Model 객체 사용
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


import java.util.List;

// 웹 페이지를 반환하는 컨트롤러임을 명시
@Controller
public class ClientController {
    private final ClientService service;

    public ClientController(ClientService service) {
        this.service = service;
    }

    @GetMapping("/clients")
    public String list(Model m) {
        List<ClientDTO> list = service.getAllPartners();
        m.addAttribute("clients", list);
        System.out.println(list);
        return "client/client_list";
    }

    @GetMapping("/clients/{id}")
    public String detail(@PathVariable("id") int id, Model m) {
        ClientDTO client = service.getPartnerDetails(id);
        m.addAttribute("client", client);
        return "clients/detail";
    } 
}
