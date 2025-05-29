package kr.co.codea.order;

import kr.co.codea.order.OrderDto;

import java.util.List;
import java.util.Map;

public interface OrderService {
	List<OrderDto> getOrderList(Map<String, Object> params);
    int getOrderCount(Map<String, Object> params);
}
