package kr.co.codea.receiving;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
public class ReceivingDAO implements ReceivingService {
	
	@Autowired
	private ReceivingMapper mp;
	
	@Override
	public List<ReceivingDTO> rec_list(String itemType, String field, String keyword) {	// 목록 페이지
		List<ReceivingDTO> list = this.mp.rec_list(itemType, field, keyword);
		
		return list;
	}
	
	@Override
	public ReceivingDTO rec_detail(int inoutId, int sourceDocType) {	// 상세 페이지
		ReceivingDTO detail = this.mp.rec_detail(inoutId, sourceDocType);
		
		return detail;
	}
	
	@Override
	public List<ReceivingDTO> rec_sea_item(int sourceDocType, String docDate) {	// 등록 페이지 제품 검색
		List<ReceivingDTO> sea_item = this.mp.rec_sea_item(sourceDocType, docDate);
		
		return sea_item;
	}
	
	@Override
	public List<ReceivingDTO> rec_sea_wh(String whName) {	// 등록 페이지 창고 검색
		List<ReceivingDTO> sea_wh = this.mp.rec_sea_wh(whName);
		
		return sea_wh;
	}
	
	@Override
	public List<ReceivingDTO> rec_sea_emp(String empName) {	// 등록 페이지 담당자 검색
		List<ReceivingDTO> sea_emp = this.mp.rec_sea_emp(empName);
		
		return sea_emp;
	}
}