package kr.co.codea.client;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/client")
public class ClientSearchApi { 
	/*서비스 의존성 주입
	 @Autowired : 이 필드(또는 생성자 메서드)에 해당하는 빈을 찾아서 주입해줘라고 지시
	 **/
	private final ClientService service;

	public ClientSearchApi(ClientService service) {
		this.service = service;
	}
	
	@GetMapping
	public ResponseEntity<List<ClientDTO>> serachClient(
			@ModelAttribute ClientDTO dto){
		
		
		
		
		return ResponseEntity.ok(null);
	}
 

}
