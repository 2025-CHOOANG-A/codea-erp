package kr.co.codea.client;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClientServiceImpl implements ClientService {
    private final ClientMapper mapper;

    public ClientServiceImpl(ClientMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<ClientDTO> getAllPartners() {
        return mapper.selectAllPartners();
    }

    @Override
    @Transactional(readOnly = true)
    public ClientDTO getPartnerDetails(Integer bpId) {
        return mapper.selectPartnerWithContacts(bpId);
    }

	@Override
	@Transactional(readOnly = true)
	public List<CommonCodeDTO> findCommonCode(String codeType, String query) {
		return mapper.findCommonCode(codeType, query);
	}

	@Override
	public int insertClient(ClientDTO dto) {
		return mapper.insertClient(dto);
	}


}
