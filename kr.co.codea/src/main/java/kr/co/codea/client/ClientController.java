package kr.co.codea.client;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.pagehelper.PageInfo;

@Controller
public class ClientController {
    private final ClientService service;

    public ClientController(ClientService service) {
        this.service = service;
    }

    //거래처 리스트 페이지 + 검색기능 + 페이징
    @GetMapping("/client")
    public String list(Model m, ClientDTO dto,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        // 페이징 처리된 거래처 목록 조회
        PageInfo<ClientDTO> pageInfo = service.searchClientbyKeyword(dto, page, size);

        // 모델에 데이터 추가
        m.addAttribute("clients", pageInfo.getList());
        m.addAttribute("searchDto", dto);
        m.addAttribute("pageInfo", pageInfo);

        // 템플릿 정보 설정
        m.addAttribute("templateName", "client/client_list");
        m.addAttribute("fragmentName", "contentFragment");

        return "client/client_default";
    }

    //거래처 상세 페이지 (기존 방식 유지 - 필요시)
    @GetMapping("/client/{id}")
    public String detail(@PathVariable("id") Integer id, Model m) {
        try {
            ClientDTO partnerInfo = service.getPartnerDetails(id);
            if (partnerInfo == null) {
                return "redirect:/client"; // 거래처가 없으면 목록으로 리다이렉트
            }

            m.addAttribute("client", partnerInfo);
            m.addAttribute("templateName", "client/client_detail");
            m.addAttribute("fragmentName", "contentFragment");

            return "client/client_default";
        } catch (Exception e) {
            // 오류 발생 시 목록 페이지로 리다이렉트
            return "redirect:/client";
        }
    }

    //거래처 등록 폼
    @GetMapping("/client/write")
    public String writeForm() {
        return "client/client_write";
    }

    //거래처 수정 폼 페이지 (기존 방식 유지 - 필요시)
    @GetMapping("/client/{id}/edit")
    public String editForm(@PathVariable("id") Integer id, Model m) {
        try {
            ClientDTO client = service.getPartnerDetails(id);
            if (client == null) {
                return "redirect:/client";
            }

            m.addAttribute("client", client);
            m.addAttribute("templateName", "client/client_edit");
            m.addAttribute("fragmentName", "contentFragment");

            return "client/client_default";
        } catch (Exception e) {
            return "redirect:/client";
        }
    }
}