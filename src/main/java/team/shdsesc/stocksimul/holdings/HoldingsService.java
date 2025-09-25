package team.shdsesc.stocksimul.holdings;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import team.shdsesc.stocksimul.market.repository.ReportRepository;
import team.shdsesc.stocksimul.market.service.DbMarketService;
import team.shdsesc.stocksimul.util.FormatUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class HoldingsService {
    private final HoldingsRepository holdingsRepository;
    private final ReportRepository reportRepository;
    private final DbMarketService dbMarketService;
    private final FormatUtil formatUtil;
    private static final String STOCK_IMG_LINK = "https://financialmodelingprep.com/image-stock/";
    private static final String STOCK_IMG_EXTENSION = ".png";

    public PortfolioResponseDTO getPortfolio(Long usersProfileId, String prevProcessDate, String processDate) {
        log.info("getPortfolio 호출 - prevDate: {}, processDate: {}", prevProcessDate, processDate);

        // 이전 날짜의 보유 주식 리스트
        HoldingsResponseDTO prevHoldingsList = getHoldingsList(usersProfileId, prevProcessDate);
        List<HoldingsDTO> prevHoldingsDTOList = prevHoldingsList.getHoldingsResponseDTOS();

        // 다음 유효 거래일 찾기
        String effectiveDate = processDate;
        try {
            Map<String, Object> result = dbMarketService.findNextEffectiveDate(LocalDate.parse(processDate), 30);
            effectiveDate = result.getOrDefault("effectiveDate", processDate).toString();
        } catch (Exception e) {
            log.warn("유효 거래일 조회 실패, processDate 사용: {}", processDate, e);
        }

        // 현재 날짜의 보유 주식 리스트
        HoldingsResponseDTO currentHoldingsList = getHoldingsList(usersProfileId, effectiveDate);
        List<HoldingsDTO> currentHoldingsDTOList = currentHoldingsList.getHoldingsResponseDTOS();

        // 변동 사항 계산
        List<ChangeDTO> changeDTOS = calculateChanges(prevHoldingsDTOList, currentHoldingsDTOList);

        log.info("이전 holdings: {}, 현재 holdings: {}, 변동사항: {}",
                prevHoldingsDTOList.size(), currentHoldingsDTOList.size(), changeDTOS.size());

        return PortfolioResponseDTO.builder()
                .holdingsDTOList(currentHoldingsDTOList) // 현재 보유 주식 반환
                .totalCurrentPrice(currentHoldingsList.getTotalCurrentPrice())
                .changeList(changeDTOS) // 변동 사항 추가
                .build();
    }

    /**
     * 이전과 현재 보유 주식 비교하여 변동 사항 계산
     */
    private List<ChangeDTO> calculateChanges(List<HoldingsDTO> prevHoldings, List<HoldingsDTO> currentHoldings) {
        List<ChangeDTO> changes = new ArrayList<>();

        // 현재 보유 주식을 Map으로 변환 (ticker 기준)
        Map<String, HoldingsDTO> currentHoldingsMap = currentHoldings.stream()
                .collect(Collectors.toMap(HoldingsDTO::getTicker, dto -> dto));

        // 이전 보유 주식을 Map으로 변환 (ticker 기준)
        Map<String, HoldingsDTO> prevHoldingsMap = prevHoldings.stream()
                .collect(Collectors.toMap(HoldingsDTO::getTicker, dto -> dto));

        // 모든 종목에 대해 변동 사항 계산
        for (HoldingsDTO current : currentHoldings) {
            String ticker = current.getTicker();
            HoldingsDTO prev = prevHoldingsMap.get(ticker);

            if (prev != null) {
                // 기존 보유 종목의 변동 계산
                Double changeAmount = formatUtil.changePriceFormatter(current.getPrice() - prev.getPrice());
                Double changeRate = prev.getPrice() != null && prev.getPrice() != 0.0
                        ? formatUtil.changePriceFormatter((changeAmount / prev.getPrice()) * 100)
                        : 0.0;

                changes.add(ChangeDTO.builder()
                        .ticker(ticker)
                        .name(current.getName())
                        .prevPrice(prev.getPrice())
                        .currentPrice(current.getPrice())
                        .changeAmount(changeAmount)
                        .changeRate(changeRate)
                        .quantity(current.getQuantity())
                        .logo(current.getLogo())
                        .build());
            } else {
                // 새로 추가된 종목 (이전에 없던 종목)
                changes.add(ChangeDTO.builder()
                        .ticker(ticker)
                        .name(current.getName())
                        .prevPrice(0.0)
                        .currentPrice(current.getPrice())
                        .changeAmount(current.getPrice())
                        .changeRate(100.0) // 신규 추가이므로 100% 증가로 표시
                        .quantity(current.getQuantity())
                        .logo(current.getLogo())
                        .build());
            }
        }

        // 매도된 종목 처리 (현재는 없지만 이전에 있던 종목)
        for (HoldingsDTO prev : prevHoldings) {
            String ticker = prev.getTicker();
            if (!currentHoldingsMap.containsKey(ticker)) {
                // 완전 매도된 종목
                changes.add(ChangeDTO.builder()
                        .ticker(ticker)
                        .name(prev.getName())
                        .prevPrice(prev.getPrice())
                        .currentPrice(0.0)
                        .changeAmount(-prev.getPrice())
                        .changeRate(-100.0) // 완전 매도이므로 -100%
                        .quantity(0L)
                        .logo(prev.getLogo())
                        .build());
            }
        }

        return changes;
    }

    public HoldingsDTO toHoldingsResponseDTOS(HoldingsEntity holdingsEntity, LocalDateTime date) {
        // 현재 주가 (1주당)
        Double currentPrice = reportRepository.findClosesPrice(date, holdingsEntity.getStock().getStockId());
        if (currentPrice == null) {
            // 같은 날 종가가 없으면 이후 첫 거래일 종가로 시도
            currentPrice = reportRepository
                    .findFirstByIdStockIdAndIdDateGreaterThanEqualOrderByIdDateAsc(
                            holdingsEntity.getStock().getStockId(), date)
                    .map(team.shdsesc.stocksimul.market.entity.Report::getClose)
                    .orElse(null);
        }
        if (currentPrice == null) {
            // 그래도 없으면 이전 마지막 거래일 종가로 시도
            currentPrice = reportRepository
                    .findFirstByIdStockIdAndIdDateLessThanEqualOrderByIdDateDesc(
                            holdingsEntity.getStock().getStockId(), date)
                    .map(team.shdsesc.stocksimul.market.entity.Report::getClose)
                    .orElse(null);
        }
        if (currentPrice == null) {
            currentPrice = 0.0;
        }

        // 구매시 총 투자금액
        Double totalInvested = holdingsEntity.getTotalPrice();

        // 보유 수량
        Long quantity = holdingsEntity.getQuantity();

        // 현재 총 평가금액
        Double currentTotalValue = formatUtil.changePriceFormatter(quantity * currentPrice);

        // 평가손익 (현재가치 - 투자금액)
        Double profitLoss = formatUtil.changePriceFormatter(currentTotalValue - totalInvested);

        // 수익률 (%) = (평가손익 / 투자금액) * 100
        Double returnRate = totalInvested != null && totalInvested != 0.0
                ? formatUtil.changePriceFormatter((profitLoss / totalInvested) * 100)
                : 0.0;

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
                .mapToDouble(HoldingsDTO::getPrice)
                .sum();

        return HoldingsResponseDTO.builder()
                .holdingsResponseDTOS(holdingsDTOList)
                .totalCurrentPrice(totalCurrentPrice)
                .build();
    }
}