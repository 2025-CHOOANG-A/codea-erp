package kr.co.codea.shipment;

import java.util.List;

public interface ShipmentService {
	public List<ShipmentDTO> ship_list(Integer sourceDocType, String field, String keyword);	// 목록 페이지
	
	public ShipmentDTO ship_detail(int inoutId, int sourceDocType);	// 상세 페이지
	
	public List<ShipmentDTO> ship_sea_item(int sourceDocType, String docDate);	// 등록 페이지 제품 검색
	
	public List<ShipmentDTO> ship_sea_wh(String whName);	// 등록 페이지 창고 검색
	
	public List<ShipmentDTO> ship_sea_emp(String empName);	// 등록 페이지 담당자 검색

	public String check(int sourceDocType, int sourceDocHeaderId, int itemId);	// 출고 중복 체크
	
	public Integer ship_insert(ShipmentDTO dto);	// 출고 등록
}