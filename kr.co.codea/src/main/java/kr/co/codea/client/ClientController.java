package kr.co.codea.client;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller; 
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
public class ClientController {
    private final ClientService service;

    public ClientController(ClientService service) {
        this.service = service;
    }

    //거래처 리스트
    @GetMapping("/client")
    public String list(Model m) {
        List<ClientDTO> list = service.getAllPartners();
        m.addAttribute("clients", list);
        m.addAttribute("templateName", "client/client_list");
        m.addAttribute("fragmentName", "contentFragment");
        System.out.println(list);
        return "client/client_default";
    }

    //거래처 리스트 상세
    @GetMapping("/client/{id}")
    public String detail(@PathVariable("id") Integer id, Model m) {
        ClientDTO parterInfo = service.getPartnerDetails(id);
        m.addAttribute("client", parterInfo);
        m.addAttribute("templateName", "client/client_detail");
        m.addAttribute("fragmentName", "contentFragment");
        return "client/client_default";
    } 
    
    //거래처 추가
    @GetMapping("/client/write")
    public String write() {
        return "client/client_write";
    } 
    
    @PostMapping("/client/client_register")
    public String client_register(@ModelAttribute(name = "dto") ClientDTO dto, RedirectAttributes ra) {
    	int result = service.insertClient(dto);
    	
    	if (result > 0) {
            ra.addFlashAttribute("","");
            return "redirect:/client"; 
        } else {
        	 ra.addFlashAttribute("error", "거래처 등록에 실패했습니다. 다시 시도해주세요.");
            return "redirect:/client/write";
        }
    	
    }
    // 거래처 업데이트
    @PutMapping("/client/{id}")
    @ResponseBody // JSON 응답을 위해 @ResponseBody 사용
    public ResponseEntity<?> updateClient(@PathVariable("id") Integer id, @RequestBody ClientDTO clientDTO) {
        // URL의 ID와 DTO의 ID가 일치하는지 확인 (보안 강화)
        if (!id.equals(clientDTO.getBpId())) {
            return new ResponseEntity<>(Map.of("message", "ID 불일치"), HttpStatus.BAD_REQUEST);
        }
        int updatedRows = service.updateclinet(clientDTO);
        if (updatedRows > 0) {
            return new ResponseEntity<>(Map.of("message", "거래처 정보가 성공적으로 업데이트되었습니다."), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(Map.of("message", "거래처 정보 업데이트에 실패했습니다."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // 담당자 정보 업데이트 (PATCH 요청 처리)
    @PatchMapping("/client/{bpId}/contact/{bcId}")
    @ResponseBody
    public ResponseEntity<?> updateContact(@PathVariable("bpId") Integer bpId,
                                           @PathVariable("bcId") Integer bcId,
                                           @RequestBody ContactDTO contactDTO) {
        // URL의 ID와 DTO의 ID가 일치하는지 확인
        if (!bcId.equals(contactDTO.getBcId())) {
            return new ResponseEntity<>(Map.of("message", "담당자 ID 불일치"), HttpStatus.BAD_REQUEST);
        }
        // 담당자가 해당 거래처에 속하는지 확인 (선택 사항, 서비스 계층에서 유효성 검사 가능)
        contactDTO.setBpId(bpId); // URL에서 받은 bpId를 DTO에 설정

        int updatedRows = service.updateContact(contactDTO);
        if (updatedRows > 0) {
            return new ResponseEntity<>(Map.of("message", "담당자 정보가 성공적으로 업데이트되었습니다."), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(Map.of("message", "담당자 정보 업데이트에 실패했습니다."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 담당자 추가 (POST 요청 처리)
    @PostMapping("/client/{bpId}/contact")
    @ResponseBody
    public ResponseEntity<?> addContact(@PathVariable("bpId") Integer bpId, @RequestBody ContactDTO contactDTO) {
        contactDTO.setBpId(bpId); // URL에서 받은 bpId를 DTO에 설정
        int insertedRows = service.insertContact(contactDTO);
        if (insertedRows > 0) {
            return new ResponseEntity<>(Map.of("message", "새 담당자가 성공적으로 추가되었습니다."), HttpStatus.CREATED); // 201 Created
        } else {
            return new ResponseEntity<>(Map.of("message", "담당자 추가에 실패했습니다."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 담당자 삭제 (DELETE 요청 처리)
    @DeleteMapping("/client/{bpId}/contact/{bcId}")
    @ResponseBody
    public ResponseEntity<?> deleteContact(@PathVariable("bpId") Integer bpId,
                                           @PathVariable("bcId") Integer bcId) {
        int deletedRows = service.deleteContact(bcId, bpId);
        if (deletedRows > 0) {
            return new ResponseEntity<>(Map.of("message", "담당자가 성공적으로 삭제되었습니다."), HttpStatus.NO_CONTENT); // 204 No Content
        } else {
            return new ResponseEntity<>(Map.of("message", "담당자 삭제에 실패했습니다."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
}
