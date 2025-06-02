package kr.co.codea.client;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/client")
public class ClientApiController {
    
    private final ClientService service;
    
    public ClientApiController(ClientService service) {
        this.service = service;
    }
    
    //거래처 상세 정보 조회 - DTO 그대로 반환
    @GetMapping("/{id}")
    public ResponseEntity<?> getClientDetail(@PathVariable("id") Integer id) {
        try {
            ClientDTO client = service.getPartnerDetails(id);
            if (client == null) {
                return new ResponseEntity<>(
                    Map.of("message", "거래처를 찾을 수 없습니다."), 
                    HttpStatus.NOT_FOUND
                );
            }
            
            // DTO를 그대로 반환 - Jackson이 자동으로 JSON 변환
            return new ResponseEntity<>(client, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(
                Map.of("message", "거래처 정보 조회 중 오류가 발생했습니다: " + e.getMessage()), 
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    
    //거래처 등록
    @PostMapping
    public ResponseEntity<?> registerClient(@RequestBody ClientDTO clientDTO) {
        try {
            int result = service.insertClient(clientDTO);
            
            if (result > 0) {
                return new ResponseEntity<>(
                    Map.of("message", "거래처가 성공적으로 등록되었습니다."), 
                    HttpStatus.CREATED
                );
            } else {
                return new ResponseEntity<>(
                    Map.of("message", "거래처 등록에 실패했습니다."), 
                    HttpStatus.INTERNAL_SERVER_ERROR
                );
            }
        } catch (Exception e) {
            return new ResponseEntity<>(
                Map.of("message", "거래처 등록 중 오류가 발생했습니다: " + e.getMessage()), 
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    
    //거래처 수정
    @PutMapping("/{id}")
    public ResponseEntity<?> updateClient(@PathVariable("id") Integer id, @RequestBody ClientDTO clientDTO) {
        try {
            // URL의 ID를 DTO에 설정
            clientDTO.setBpId(id);
            
            int updatedRows = service.updateClient(clientDTO);
            if (updatedRows > 0) {
                return new ResponseEntity<>(
                    Map.of("message", "거래처 정보가 성공적으로 업데이트되었습니다."), 
                    HttpStatus.OK
                );
            } else {
                return new ResponseEntity<>(
                    Map.of("message", "거래처 정보 업데이트에 실패했습니다."), 
                    HttpStatus.INTERNAL_SERVER_ERROR
                );
            }
        } catch (Exception e) {
            return new ResponseEntity<>(
                Map.of("message", "거래처 수정 중 오류가 발생했습니다: " + e.getMessage()), 
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    
    //거래처 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteClient(@PathVariable("id") Integer id) {
        try {
            int deletedRows = service.deleteClient(id);
            if (deletedRows > 0) {
                return new ResponseEntity<>(
                    Map.of("message", "거래처가 성공적으로 삭제되었습니다."), 
                    HttpStatus.OK
                );
            } else {
                return new ResponseEntity<>(
                    Map.of("message", "거래처 삭제에 실패했습니다."), 
                    HttpStatus.INTERNAL_SERVER_ERROR
                );
            }
        } catch (Exception e) {
            return new ResponseEntity<>(
                Map.of("message", "거래처 삭제 중 오류가 발생했습니다: " + e.getMessage()), 
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
   
    //거래처 담당자 목록 조회 - DTO List 그대로 반환
    @GetMapping("/{id}/contacts")
    public ResponseEntity<List<ContactDTO>> getClientContacts(@PathVariable("id") Integer bpId) {
        try {
            List<ContactDTO> contacts = service.getContactsByBpId(bpId);
            return new ResponseEntity<>(contacts, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    //담당자 등록
    @PostMapping("/{bpId}/contact")
    public ResponseEntity<?> addContact(@PathVariable("bpId") Integer bpId, @RequestBody ContactDTO contactDTO) {
        try {
            contactDTO.setBpId(bpId); // URL에서 받은 bpId를 DTO에 설정
            int insertedRows = service.insertContact(contactDTO);
            if (insertedRows > 0) {
                return new ResponseEntity<>(
                    Map.of("message", "새 담당자가 성공적으로 추가되었습니다."), 
                    HttpStatus.CREATED
                );
            } else {
                return new ResponseEntity<>(
                    Map.of("message", "담당자 추가에 실패했습니다."), 
                    HttpStatus.INTERNAL_SERVER_ERROR
                );
            }
        } catch (Exception e) {
            return new ResponseEntity<>(
                Map.of("message", "담당자 등록 중 오류가 발생했습니다: " + e.getMessage()), 
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    
    //담당자 수정
    @PatchMapping("/{bpId}/contact/{bcId}")
    public ResponseEntity<?> updateContact(@PathVariable("bpId") Integer bpId,
                                           @PathVariable("bcId") Integer bcId,
                                           @RequestBody ContactDTO contactDTO) {
        try {
            // URL의 ID를 DTO에 설정
            contactDTO.setBcId(bcId);
            contactDTO.setBpId(bpId);

            int updatedRows = service.updateContact(contactDTO);
            if (updatedRows > 0) {
                return new ResponseEntity<>(
                    Map.of("message", "담당자 정보가 성공적으로 업데이트되었습니다."), 
                    HttpStatus.OK
                );
            } else {
                return new ResponseEntity<>(
                    Map.of("message", "담당자 정보 업데이트에 실패했습니다."), 
                    HttpStatus.INTERNAL_SERVER_ERROR
                );
            }
        } catch (Exception e) {
            return new ResponseEntity<>(
                Map.of("message", "담당자 수정 중 오류가 발생했습니다: " + e.getMessage()), 
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    
    //담당자 삭제
    @DeleteMapping("/{bpId}/contact/{bcId}")
    public ResponseEntity<?> deleteContact(@PathVariable("bpId") Integer bpId,
                                           @PathVariable("bcId") Integer bcId) {
        try {
            int deletedRows = service.deleteContact(bcId, bpId);
            if (deletedRows > 0) {
                return new ResponseEntity<>(
                    Map.of("message", "담당자가 성공적으로 삭제되었습니다."), 
                    HttpStatus.OK
                );
            } else {
                return new ResponseEntity<>(
                    Map.of("message", "담당자 삭제에 실패했습니다."), 
                    HttpStatus.INTERNAL_SERVER_ERROR
                );
            }
        } catch (Exception e) {
            return new ResponseEntity<>(
                Map.of("message", "담당자 삭제 중 오류가 발생했습니다: " + e.getMessage()), 
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}