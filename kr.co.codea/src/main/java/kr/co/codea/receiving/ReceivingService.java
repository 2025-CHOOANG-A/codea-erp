package kr.co.codea.receiving;

import java.util.List;

public interface ReceivingService {
	public List<ReceivingDTO> rec_list(Integer sourceDocType, String field, String keyword);	// 목록 페이지
	
	public ReceivingDTO rec_detail(int inoutId, int sourceDocType);	// 상세 페이지
	
	public List<ReceivingDTO> rec_sea_item(int sourceDocType, String docDate);	// 등록 페이지 제품 검색
	
	public List<ReceivingDTO> rec_sea_wh(String whName);	// 등록 페이지 창고 검색
	
	public List<ReceivingDTO> rec_sea_emp(String empName);	// 등록 페이지 담당자 검색
	
	public String check(int sourceDocType, int sourceDocHeaderId, int itemId);	// 입고 중복 체크
	
	public Integer rec_insert(ReceivingDTO dto);	// 입고 등록
}