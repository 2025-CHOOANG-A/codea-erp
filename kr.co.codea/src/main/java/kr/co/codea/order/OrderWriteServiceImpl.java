package kr.co.codea.order;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderWriteServiceImpl implements OrderWriteService {

    private final OrderWriteMapper writeMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrder(OrderWriteDto.FormOrderCreateRequest request) throws Exception {
        log.info("주문 등록 시작 - bpId: {}, empId: {}, 상세 건수: {}", 
                 request.getBpId(), request.getEmpId(), 
                 request.getDetailList() != null ? request.getDetailList().size() : 0);
        
        // 1) 헤더 INSERT 전 검증
        if (request.getBpId() == null || request.getEmpId() == null) {
            throw new IllegalArgumentException("발주처ID와 담당자ID는 필수입니다.");
        }
        
        // 2) 헤더 INSERT
        log.info("주문 헤더 INSERT 시작");
        int headerRows = writeMapper.insertOrderHeader(request);
        log.info("주문 헤더 INSERT 결과 - affected rows: {}, 생성된 ordId: {}", 
                 headerRows, request.getOrdId());
        
        if (headerRows != 1) {
            throw new Exception("주문 헤더 저장에 실패했습니다. affected rows: " + headerRows);
        }
        
        Long newOrdId = request.getOrdId();
        if (newOrdId == null) {
            throw new Exception("주문 헤더 저장 후 생성된 ID를 가져올 수 없습니다.");
        }
        
        // 3) 상세 INSERT
        if (request.getDetailList() == null || request.getDetailList().isEmpty()) {
            throw new IllegalArgumentException("최소 한 건의 주문 상세를 입력해야 합니다.");
        }
        
        log.info("주문 상세 INSERT 시작 - ordId: {}, 상세 건수: {}", 
                 newOrdId, request.getDetailList().size());
        
        int detailIndex = 0;
        for (OrderWriteDto.FormOrderDetailRequest detail : request.getDetailList()) {
            // 상세별 검증
            if (detail.getItemId() == null || detail.getOrderQty() == null || 
                detail.getOrderQty() <= 0 || detail.getUnitPrice() == null) {
                throw new IllegalArgumentException(
                    String.format("상세 %d번째: 품목ID, 수량, 단가는 필수입니다.", detailIndex + 1));
            }
            
            detail.setOrdId(newOrdId);
            log.info("상세 INSERT - index: {}, itemId: {}, qty: {}, price: {}", 
                     detailIndex, detail.getItemId(), detail.getOrderQty(), detail.getUnitPrice());
            
            int detailRows = writeMapper.insertOrderDetail(detail);
            log.info("상세 INSERT 결과 - index: {}, affected rows: {}", detailIndex, detailRows);
            
            if (detailRows != 1) {
                throw new Exception(String.format("주문 상세 %d번째 저장 중 오류가 발생했습니다. affected rows: %d", 
                                                  detailIndex + 1, detailRows));
            }
            detailIndex++;
        }
        
        log.info("주문 등록 완료 - ordId: {}, 헤더: {}건, 상세: {}건", 
                 newOrdId, headerRows, detailIndex);
    }

    @Override
    public List<OrderWriteDto.ItemSimple> searchItems(String keyword) {
        log.debug("품목 검색 - keyword: '{}'", keyword);
        List<OrderWriteDto.ItemSimple> result = writeMapper.searchItems(keyword);
        log.debug("품목 검색 결과 - {}건", result.size());
        return result;
    }

    @Override
    public List<OrderWriteDto.PartnerSimple> searchPartners(String keyword) {
        log.debug("발주처 검색 - keyword: '{}'", keyword);
        List<OrderWriteDto.PartnerSimple> result = writeMapper.searchPartners(keyword);
        log.debug("발주처 검색 결과 - {}건", result.size());
        return result;
    }

    @Override
    public List<OrderWriteDto.EmployeeSimple> searchEmployees(String keyword) {
        log.debug("담당자 검색 - keyword: '{}'", keyword);
        List<OrderWriteDto.EmployeeSimple> result = writeMapper.searchEmployees(keyword);
        log.debug("담당자 검색 결과 - {}건", result.size());
        return result;
    }
}