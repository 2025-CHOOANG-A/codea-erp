package kr.co.codea.productactual;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import kr.co.codea.productplan.ProductPlanDTO;
import kr.co.codea.receiving.ReceivingDAO;
import kr.co.codea.receiving.ReceivingDTO;
import kr.co.codea.receiving.ReceivingMapper;

@Service
public class ProductActualServiceimp implements ProductActualService {
	
	   private final ProductActualMapper mapper;
	    private final ReceivingDAO receivingDAO; // 입고 처리용
	    
	    public ProductActualServiceimp(ProductActualMapper mapper, ReceivingDAO receivingDAO) {
	        this.mapper = mapper;
	        this.receivingDAO = receivingDAO;
	    }

	@Override
	public List<ProductActualDTO> productActualList(ProductActualDTO dto) {
		return mapper.productActualList(dto);
	}

	
	
	
	//일일생산실적 등록
	@Override
	public void registerProductActual(ProductActualDTO dto) {
        if (dto.getPlanId() == null || dto.getPlanId().isEmpty()) {
            throw new IllegalArgumentException("생산 계획 ID는 필수입니다.");
        }
        if (dto.getActualDate() == null) {
            throw new IllegalArgumentException("실적 일자는 필수입니다.");
        }
        if (dto.getActualQty() == null || dto.getActualQty() < 0) {
            throw new IllegalArgumentException("생산 수량은 0 이상이어야 합니다.");
        }
        if (dto.getDefectQty() == null || dto.getDefectQty() < 0) {
            // defectQty가 null이거나 음수이면 기본값 0으로 설정하거나 오류 처리
            dto.setDefectQty(0);
            // throw new IllegalArgumentException("불량 수량은 0 이상이어야 합니다.");
        }
        try {
    		mapper.registerProductActual(dto);
			mapper.updateProductionPlanActualQty(dto.getPlanId()); //일일생산량을 생산계획에 반영

		} catch (Exception e) {
			throw new RuntimeException("생산 실적 DB 처리 중 오류 발생: " + e.getMessage(), e);
		}
	}

	@Override
	public List<ProductActualDTO> getDailyActualsByPlanId(String planId) {
		return mapper.getDailyActualsByPlanId(planId);
	}

	@Override
	public PageInfo<ProductActualDTO> getpages(ProductActualDTO dto, int page, int size) {
		PageHelper.startPage(page, size);
		List<ProductActualDTO> list = mapper.productActualList(dto);
		
		return new PageInfo<>(list);
	}

	@Override
    @Transactional
    public void startWork(String planId) {
        if (planId == null || planId.isEmpty()) {
            throw new IllegalArgumentException("생산계획 ID는 필수입니다.");
        }
        
        int result = mapper.startWork(planId);
        if (result == 0) {
            throw new RuntimeException("작업 시작에 실패했습니다. 생산계획이 '작업지시' 상태인지 확인해주세요.");
        }
    
	}

	@Override
	   @Transactional
	    public void endWork(String planId) {
	        if (planId == null || planId.isEmpty()) {
	            throw new IllegalArgumentException("생산계획 ID는 필수입니다.");
	        }
	        
	        try {
	            // 1. 작업 종료 (상태를 '완료'로 변경)
	            int result = mapper.endWork(planId);
	            if (result == 0) {
	                throw new RuntimeException("작업 종료에 실패했습니다. 생산계획이 '진행중' 상태인지 확인해주세요.");
	            }
	            
	            // 2. 완제품 입고 처리
	            processProductReceiving(planId);
	            
	        } catch (Exception e) {
	            throw new RuntimeException("작업 종료 처리 중 오류 발생: " + e.getMessage(), e);
	        }
	    }
	
    /**
     * 완제품 입고 처리
     */
    private void processProductReceiving(String planId) {
        // 생산계획 정보 조회
        ProductActualDTO planInfo = mapper.getProductionPlanForReceiving(planId);
        
        if (planInfo == null || planInfo.getActualQty() == null || planInfo.getActualQty() <= 0) {
            throw new RuntimeException("입고할 완제품 수량이 없습니다.");
        }
        
        // 입고 데이터 생성
        ReceivingDTO receivingDto = new ReceivingDTO();
        receivingDto.setInoutType(22); // 생산입고
        receivingDto.setItemId(planInfo.getItemId()); // Item ID
        receivingDto.setWhId(81); // 완제품창고 ID
        receivingDto.setQuantity(planInfo.getActualQty()); // 실제 생산수량
        receivingDto.setItemUnitCost(planInfo.getPrice() != null ? planInfo.getPrice() : 0); // 품목 단가
        receivingDto.setSourceDocType(44); // 재고이동
        receivingDto.setSourceDocHeaderId(planInfo.getPlanNo()); // 생산계획 ID (숫자만)
        receivingDto.setSourceDocDetailId(0); // 상세 ID (없음)
        receivingDto.setEmpId(1); // 시스템 자동 처리 담당자 (실제로는 현재 로그인 사용자)
        receivingDto.setRemark("생산완료 자동입고 - " + planId);
        
        // 입고 등록
        Integer insertResult = receivingDAO.rec_insert(receivingDto);
        if (insertResult <= 0) {
            throw new RuntimeException("완제품 입고 처리에 실패했습니다.");
        }
    }
  
	

}
