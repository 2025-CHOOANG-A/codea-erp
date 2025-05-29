package kr.co.codea.order;

import kr.co.codea.order.OrderDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    List<OrderDto> selectOrderList(Map<String, Object> params);
    int countOrders(Map<String, Object> params);
}