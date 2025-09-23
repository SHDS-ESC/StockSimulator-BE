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

        // 금일 주식 가격 (1주)
        Double avgPrice = reportRepository.findClosesPrice(date, holdingsEntity.getStock().getStockId());

        // 가저온 실 구매가
        Double price = holdingsEntity.getTotalPrice();

        // 구매량
        Long quantity = holdingsEntity.getQuantity();

        // 주당 총 주가 (금일 기준 반영)
        Double totalPrice = formatUtil.changePriceFormatter(quantity * avgPrice);

        // 등락률
        double changeRate = formatUtil.changePriceFormatter(totalPrice / price - 1);
        // 등락가
        double changeAmount = formatUtil.changePriceFormatter(totalPrice - price);

        return HoldingsDTO.builder()
                .ticker(holdingsEntity.getStock().getTicker())
                .name(holdingsEntity.getStock().getName())
                .price(totalPrice)
                .quantity(holdingsEntity.getQuantity())
                .change(changeRate)
                .changeAmount(changeAmount)
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
