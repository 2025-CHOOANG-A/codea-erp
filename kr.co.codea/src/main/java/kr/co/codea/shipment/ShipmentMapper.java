package kr.co.codea.shipment;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ShipmentMapper {
	public List<ShipmentDTO> ship_list(@Param("sourceDocType") Integer sourceDocType, @Param("field") String field, @Param("keyword") String keyword);	// 목록 페이지
	
	public ShipmentDTO ship_detail(@Param("inoutId") int inoutId, @Param("sourceDocType") int sourceDocType);	// 상세 페이지
	
	public List<ShipmentDTO> ship_sea_item(@Param("sourceDocType") int sourceDocType, @Param("docDate") String docDate);	// 등록 페이지 제품 검색
	
	public List<ShipmentDTO> ship_sea_wh(@Param("whName") String whName);	// 등록 페이지 창고 검색
	
	public List<ShipmentDTO> ship_sea_emp(@Param("empName") String empName);	// 등록 페이지 담당자 검색

	public Integer ship_check(@Param("sourceDocType") int sourceDocType, @Param("sourceDocHeaderId") int sourceDocHeaderId, @Param("itemId") int itemId);	// 출고 중복 체크
	
	public Integer ship_insert(ShipmentDTO dto);	// 출고 등록
}