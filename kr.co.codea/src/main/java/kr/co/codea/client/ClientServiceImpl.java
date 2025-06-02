package kr.co.codea.client;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

@Service
@Transactional
public class ClientServiceImpl implements ClientService {
    
    private final ClientMapper mapper;
    
    public ClientServiceImpl(ClientMapper mapper) {
        this.mapper = mapper;
    }
    

    // 거래처
    @Override
    public List<ClientDTO> getAllPartners() {
        return mapper.getAllPartners();
    }
    
    @Override
    public ClientDTO getPartnerDetails(Integer bpId) {
        ClientDTO client = mapper.getPartnerDetails(bpId);
        
        // 업태 코드명 설정
        if (client != null && client.getBizCond() != null) {
            String bizCondCode = mapper.getBizCondCodeName(client.getBizCond());
            client.setBizCondCode(bizCondCode);
        }
        
        return client;
    }
    
    @Override
    public PageInfo<ClientDTO> searchClientbyKeyword(ClientDTO dto, int page, int size) {
        // 페이징 설정
        PageHelper.startPage(page, size);
        
        // 담당자 수를 포함한 검색 수행 (DTO 직접 전달)
        List<ClientDTO> list = mapper.searchClientbyKeywordWithContactCount(dto);
        
        // 각 거래처의 업태 코드명 설정
        for (ClientDTO client : list) {
            if (client.getBizCond() != null) {
                String bizCondCode = mapper.getBizCondCodeName(client.getBizCond());
                client.setBizCondCode(bizCondCode);
            }
        }
        
        return new PageInfo<>(list);
    }
    
    @Override
    public int insertClient(ClientDTO client) {
        return mapper.insertClient(client);
    }
    
    @Override
    public int updateClient(ClientDTO client) {
        return mapper.updateClient(client);
    }
    
    @Override
    @Transactional
    public int deleteClient(Integer bpId) {
        // 1. 먼저 관련 담당자들을 삭제
        mapper.deleteContactsByBpId(bpId);
        
        // 2. 거래처 삭제
        return mapper.deleteClient(bpId);
    }
    
    // 담당자
    @Override
    public List<ContactDTO> getContactsByBpId(Integer bpId) {
        return mapper.getContactsByBpId(bpId);
    }
    
    @Override
    public int insertContact(ContactDTO contact) {
        return mapper.insertContact(contact);
    }
    
    @Override
    public int updateContact(ContactDTO contact) {
        return mapper.updateContact(contact);
    }
    
    @Override
    public int deleteContact(Integer bcId, Integer bpId) {
        return mapper.deleteContact(bcId, bpId);
    }
    
    // 공통코드 
    @Override
    public List<CommonCodeDTO> findCommonCode(String codeType, String query) {
        return mapper.findCommonCode(codeType, query);
    }
    
    @Override
    public CommonCodeDTO findCommonCodeById(Integer codeId) {
        return mapper.findCommonCodeById(codeId);
    }
}