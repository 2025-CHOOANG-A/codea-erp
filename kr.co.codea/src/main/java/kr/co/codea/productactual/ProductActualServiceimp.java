package kr.co.codea.productactual;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class ProductActualServiceimp implements ProductActualService {
	private final ProductActualMapper mapper;
	
	public ProductActualServiceimp(ProductActualMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public List<ProductActualDTO> productActualList(ProductActualDTO dto) {
		return mapper.productActualList(dto);
	}

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

		} catch (Exception e) {
			mapper.updateProductionPlanActualQty(dto.getPlanId());
		}
	}

	@Override
	public List<ProductActualDTO> getDailyActualsByPlanId(String planId) {
		return mapper.getDailyActualsByPlanId(planId);
	}

}
