package kr.co.codea.order;

/**
 * 주문 등록 관련 비즈니스 로직 인터페이스
 */
public interface OrderWriteService {
    void createOrder(OrderWriteDto.FormOrderCreateRequest request) throws Exception;
}
