package kr.co.codea.receiving;

import java.util.List;

public interface ReceivingService {
	public List<ReceivingDTO> rec_list();	// 목록 페이지
	
	public ReceivingDTO rec_detail(int inoutId);	// 상세 페이지
}