package kr.co.codea.mrp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Service
public class MrpDAO implements MrpService {
	@Autowired
	private MrpMapper mp;
	
	
}
