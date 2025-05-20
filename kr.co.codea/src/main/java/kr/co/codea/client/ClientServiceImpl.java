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
    public ClientDTO getPartnerDetails(int bpId) {
        return mapper.selectPartnerWithContacts(bpId);
    }

}
