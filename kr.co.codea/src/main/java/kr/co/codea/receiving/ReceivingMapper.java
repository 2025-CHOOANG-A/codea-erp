package kr.co.codea.receiving;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReceivingMapper {
	public List<ReceivingDTO> rec_list(@Param("sourceDocType") Integer sourceDocType, @Param("field") String field, @Param("keyword") String keyword);	// 목록 페이지
	
	public ReceivingDTO rec_detail(@Param("inoutId") int inoutId, @Param("sourceDocType") int sourceDocType);	// 상세 페이지

	public List<ReceivingDTO> rec_sea_item(@Param("sourceDocType") int sourceDocType, @Param("docDate") String docDate);	// 등록 페이지 제품 검색
	
	public List<ReceivingDTO> rec_sea_wh(@Param("whName") String whName);	// 등록 페이지 창고 검색
	
	public List<ReceivingDTO> rec_sea_emp(@Param("empName") String empName);	// 등록 페이지 담당자 검색

	public Integer rec_check(@Param("sourceDocType") int sourceDocType, @Param("sourceDocHeaderId") int sourceDocHeaderId, @Param("itemId") int itemId);	// 입고 중복 체크
	
	public Integer rec_insert(ReceivingDTO dto);	// 입고 등록
	
	List<ReceivingDTO> selectRecentReceivingList(@Param("size") int size);
}