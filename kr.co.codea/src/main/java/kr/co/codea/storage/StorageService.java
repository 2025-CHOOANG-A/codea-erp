package kr.co.codea.storage;

import java.util.List;

public interface StorageService {

	List<StorageDTO> storageListByKeyword(StorageDTO dto); //창고 리스트
	StorageDTO getStorageDetailById(Integer whId); //창고id로 상세
	int insertStorageList(StorageDTO dto ); //창고 추가


}
