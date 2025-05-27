package kr.co.codea.purchase;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import kr.co.codea.purchase.PurchaseDto;

@Mapper
public interface PurchaseMapper {
	 List<PurchaseDto> selectPurchaseList(Map<String, Object> params);
     int countPurchaseList(Map<String, Object> params); // 페이징을 위한 전체 개수
}
