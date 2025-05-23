package kr.co.codea.storage;

import java.util.List;

public interface StorageService {
	//리스트 출력
	 List<StorageDTO> storageListByKeyword(StorageDTO dto);
	 
	 //상세정보 api
	StorageDTO getStorageDetailById(Integer whId); 


}
