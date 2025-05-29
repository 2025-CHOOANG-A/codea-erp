package kr.co.codea.order;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;

    @Override
    public List<OrderDto> getOrderList(Map<String, Object> params) {
        return orderMapper.selectOrderList(params);
    }

    @Override
    public int getOrderCount(Map<String, Object> params) {
        return orderMapper.countOrders(params);
    }
}