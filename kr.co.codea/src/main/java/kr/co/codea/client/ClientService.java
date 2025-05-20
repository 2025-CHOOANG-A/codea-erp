package kr.co.codea.client;


import java.util.List;

import org.springframework.stereotype.Service;


public interface ClientService {
    List<ClientDTO> getAllPartners();
    ClientDTO getPartnerDetails(int bpId);
}