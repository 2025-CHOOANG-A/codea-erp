package kr.co.codea.storage;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class StorageServiceimp implements StorageService {
	private final StorageMapper mapper;
	
	public StorageServiceimp(StorageMapper mapper) {
		this.mapper = mapper;
	}
	

	@Override
	public List<StorageDTO> storageListByKeyword(StorageDTO dto) {
		return mapper.selectStorageList(dto);
	}


	@Override
	public StorageDTO getStorageDetailById(Integer whId) {
		return mapper.selectStorageDetailById(whId);
	}


	@Override
	public int insertStorageList(StorageDTO dto) {
		return mapper.insertStorageList(dto);
	}

}
