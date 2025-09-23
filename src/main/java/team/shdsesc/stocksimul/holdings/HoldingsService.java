package team.shdsesc.stocksimul.holdings;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import team.shdsesc.stocksimul.market.repository.ReportRepository;
import team.shdsesc.stocksimul.util.FormatUtil;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class HoldingsService {
    private final HoldingsRepository holdingsRepository;
    private final ReportRepository reportRepository;
    private final FormatUtil formatUtil;
    private static final String STOCK_IMG_LINK = "https://financialmodelingprep.com/image-stock/";
    private static final String STOCK_IMG_EXTENSION = ".png";

    public HoldingsDTO toHoldingsResponseDTOS(HoldingsEntity holdingsEntity, LocalDateTime date) {
        // 현재 주가 (1주당)
        Double currentPrice = reportRepository.findClosesPrice(date, holdingsEntity.getStock().getStockId());

        // 구매시 총 투자금액
        Double totalInvested = holdingsEntity.getTotalPrice();

        // 보유 수량
        Long quantity = holdingsEntity.getQuantity();

        // 현재 총 평가금액
        Double currentTotalValue = formatUtil.changePriceFormatter(quantity * currentPrice);

        // 평가손익 (현재가치 - 투자금액)
        Double profitLoss = formatUtil.changePriceFormatter(currentTotalValue - totalInvested);

        // 수익률 (%) = (평가손익 / 투자금액) * 100
        Double returnRate = formatUtil.changePriceFormatter((profitLoss / totalInvested) * 100);

        return HoldingsDTO.builder()
                .ticker(holdingsEntity.getStock().getTicker())
                .name(holdingsEntity.getStock().getName())
                .price(currentTotalValue)  // 현재 총 평가금액
                .quantity(holdingsEntity.getQuantity())
                .change(returnRate)       // 수익률 (%)
                .changeAmount(profitLoss) // 평가손익 ($)
                .logo(STOCK_IMG_LINK + holdingsEntity.getStock().getTicker() + STOCK_IMG_EXTENSION)
                .build();
    }

    HoldingsResponseDTO getHoldingsList(Long usersProfileId, String currentDate) {
        List<HoldingsEntity> holdingsEntityList = holdingsRepository.getHoldingsList(usersProfileId);
        List<HoldingsDTO> holdingsDTOList = holdingsEntityList
                .stream()
                .map(entity -> toHoldingsResponseDTOS(entity, formatUtil.localDateTimeFormatter(currentDate)))
                .toList();
        double totalCurrentPrice = holdingsDTOList.stream()
                .mapToDouble(HoldingsDTO::getPrice) // 필드를 꺼낸다
                .sum();

        return HoldingsResponseDTO.builder()
                .holdingsResponseDTOS(holdingsDTOList)
                .totalCurrentPrice(totalCurrentPrice)
                .build();
    }

}
