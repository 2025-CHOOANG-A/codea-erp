package kr.co.codea.client;

import org.springframework.stereotype.Controller; // @Controller 사용
import org.springframework.ui.Model; // Model 객체 사용
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

// 웹 페이지를 반환하는 컨트롤러임을 명시
@Controller
public class ClientController {
    private final ClientService service;

    public ClientController(ClientService service) {
        this.service = service;
    }

    @GetMapping("/client")
    public String list(Model m) {
        List<ClientDTO> list = service.getAllPartners();
        m.addAttribute("clients", list);
        m.addAttribute("templateName", "client/client_list");
        m.addAttribute("fragmentName", "contentFragment");
        System.out.println(list);
        return "client/client_default";
    }

    @GetMapping("/client/{id}")
    public String detail(@PathVariable("id") Integer id, Model m) {
        ClientDTO parterInfo = service.getPartnerDetails(id);
        m.addAttribute("client", parterInfo);
        m.addAttribute("templateName", "client/client_detail");
        m.addAttribute("fragmentName", "contentFragment");
        return "client/client_default";
    } 
    
    @GetMapping("/client/write")
    public String write() {
        return "client/client_write";
    } 
    
    @PostMapping("/client/client_register")
    public String client_register(@ModelAttribute(name = "dto") ClientDTO dto, RedirectAttributes ra) {
    	int result = service.insertClient(dto);
    	
    	if (result > 0) {
            // 등록 성공 시
            ra.addFlashAttribute("","");
            return "redirect:/client"; // 등록 후 목록 페이지로 리다이렉트
        } else {
            // 등록 실패 시
        	 ra.addFlashAttribute("error", "거래처 등록에 실패했습니다. 다시 시도해주세요.");
            return "redirect:/client/write"; // 실패 시 등록 폼으로 다시 리다이렉트 (입력 데이터는 유지되지 않음)
            // 또는 "client/client_write"로 포워드하여 입력 데이터를 Model에 담아 다시 보여줄 수도 있습니다.
        }
    	
    }
}
