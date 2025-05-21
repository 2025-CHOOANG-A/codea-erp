package kr.co.codea.item;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class item_service {
	
	@Autowired
	item_mapper item_mp; //item mapper 

	public List<item_DTO> item_select(){
		List<item_DTO> result = item_mp.item_select(); 
		
		return result;
		
	}
	
	
}
