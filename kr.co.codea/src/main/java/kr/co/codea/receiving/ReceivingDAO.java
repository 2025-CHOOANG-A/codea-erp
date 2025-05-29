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
	public List<ReceivingDTO> rec_list() {	// 목록 페이지
		List<ReceivingDTO> list = this.mp.rec_list();
		
		return list;
	}
	
	@Override
	public ReceivingDTO rec_detail(int inoutId) {	// 상세 페이지
		ReceivingDTO detail = this.mp.rec_detail(inoutId);
		
		return detail;
	}
}