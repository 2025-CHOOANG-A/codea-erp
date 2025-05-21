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

    	ClientDTO bpinfo = mapper.selectPartnerWithContacts(bpId);
    	if(bpinfo.getBizCond() != null) {
    		bpinfo.setBizCondCode(mapper.findCommonCodeById(bpinfo.getBizCond()).getCode());
    	}
        return bpinfo;
    }

	@Override
	@Transactional(readOnly = true)
	public List<CommonCodeDTO> findCommonCode(String codeType, String query) {
		return mapper.findCommonCode(codeType, query);
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
	public int updateContact(ContactDTO contact) {
		return mapper.updateContact(contact);
	}

	@Override
	public int insertContact(ContactDTO contact) {
		return mapper.insertContact(contact);
	}

	@Override
	public int deleteContact(Integer bcId, Integer bpId) {
		return mapper.deleteContact(bcId, bpId);
	}

	@Override
	public CommonCodeDTO findCommonCodeById(Integer codeId) {
		
		return mapper.findCommonCodeById(codeId);
	}



}
