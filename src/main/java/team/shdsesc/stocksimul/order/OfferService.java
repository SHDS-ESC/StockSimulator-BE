package team.shdsesc.stocksimul.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import team.shdsesc.stocksimul.holdings.HoldingsEntity;
import team.shdsesc.stocksimul.holdings.HoldingsRepository;
import team.shdsesc.stocksimul.market.entity.Stock;
import team.shdsesc.stocksimul.market.repository.MarketStockRepository;
import team.shdsesc.stocksimul.userprofile.UserProfileEntity;
import team.shdsesc.stocksimul.userprofile.UserProfileRepository;
import team.shdsesc.stocksimul.util.FormatUtil;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class OfferService {
    private static final String STOCK_IMG_LINK = "https://financialmodelingprep.com/image-stock/";
    private static final String STOCK_IMG_EXTENSION = ".png";
    private final OfferRepository offerRepository;
    private final UserProfileRepository userProfileRepository;
    private final MarketStockRepository marketStockRepository;
    private final HoldingsRepository holdingsRepository;
    private final FormatUtil formatUtil;

    OfferEntity toOfferEntity(OfferRequestDTO dto) {
        // 주문에 넣을 유저 프로필 가져오기ㄹ
        UserProfileEntity userProfile = userProfileRepository
                .findById(dto.usersProfileId)
                .orElseThrow(() -> new RuntimeException("usersProfileId not found"));

        // 주문에 넣을 티커 가저오기
        Stock stock = marketStockRepository
                .findByTicker(dto.stock)
                .orElseThrow(() -> new RuntimeException("stockId not found"));

        // Hodings 업데이트
        // 보유 주식 수량 (없으면 0)
        HoldingsEntity holding = holdingsRepository
                .getHoldingsStockAmount(dto.usersProfileId, stock.getStockId())
                .orElseGet(() -> holdingsRepository.save(HoldingsEntity.builder()
                        .userProfile(userProfile)
                        .stock(stock)
                        .totalPrice(0.0)
                        .modDate(LocalDateTime.now())
                        .quantity(0L)
                        .build()));

        // 보유 수량
        Long amount = holding.getQuantity(); //처음이면 0, 아니면 기존 갯수
        Double price = holding.getTotalPrice();//처음이면 0.0, 아니면 기존 총액

        // 수량 계산 (BUY는 +, SELL은 -)
        long quantity = dto.type == OfferType.BUY
                ? amount + dto.quantity // 기존 갯수에 구매 갯수 추가
                : amount - dto.quantity;// 기존 갯수에 구매 갯수 감소

        // 실 구매 가격
        double tPrice = dto.price * dto.quantity;

        // 보유자산 총합
        // price = 평단가 * 갯수 가격 총합, tPrice는 오늘 가격 * 갯수 총합
        double totalPrice = dto.type == OfferType.BUY
                ? price + tPrice // 기존 총합 금액 + 토탈 금액
                : price - price * dto.getQuantity() / amount;// 기촌 총합 금액 - 토탈 금액


        double remainCashBalance = dto.type == OfferType.BUY ?
                userProfile.getCashBalance() - tPrice : userProfile.getCashBalance() + tPrice;

        log.info("매매/매도" + dto.type + totalPrice + " " + userProfile.getCashBalance() + " " + price + " " + tPrice);

        // 수량이 0보다 작아지면 실패
        if (quantity < 0) {
            throw new RuntimeException("quantity less than 0");
        } else if (remainCashBalance < 0) {
            throw new RuntimeException("remainCashBalance less than 0");
        }

        // 0보다 크면 업데이트, 실 갯수, 평단가 * 실 개수
        holdingsRepository.updateHoldingStockAmount(dto.usersProfileId, stock.getStockId(), quantity, totalPrice);
        userProfile.setUsersCashBalance(remainCashBalance);
        userProfileRepository.save(userProfile);

        // 엔티티 빌드
        return OfferEntity.builder()
                .offerDate(dto.getOfferDate().atStartOfDay())
                .userProfile(userProfile)
                .stock(stock)
                .type(dto.type)
                .quantity(dto.quantity)
                .price(dto.price)
                .build();
    }

    void calcOfferStock(OfferRequestDTO offerRequestDTO) {
        OfferEntity offerEntity = toOfferEntity(offerRequestDTO);
        offerRepository.save(offerEntity);
    }

//    private TodayOfferDTO toTodayOfferDto(OfferEntity offerEntity) {
//            holdingsRepository.getHoldingsList()
//
//    }
//
//    public TodayOfferResponseDTO tradeTodayOffer(TodayOfferRequestDTO todayOfferRequestDTO) {
//        LocalDateTime formatDate = formatUtil.localDateTimeFormatter(todayOfferRequestDTO.offerDate.toString());
//
//        // 1) 유저의 오늘 거래 내역 가져오기
//        List<OfferEntity> todayOfferList = offerRepository.findByTodayOfferHistory(todayOfferRequestDTO.userProfileId, formatDate);
//
//        // 2) TodayOfferDTO 변환
//        List<TodayOfferDTO> todayOfferDTOS = todayOfferList.stream()
//                .map(this::toTodayOfferDto)
//                .collect(Collectors.toList());
//
//        // 3) 종목별 총액 및 변화량 계산
//        double totalPrice = 0.0;
//        double totalChangeAmount = 0.0;
//
//        for (TodayOfferDTO dto : todayOfferDTOS) {
//            totalPrice += dto.getPrice();                 // 총 거래 금액
//            totalChangeAmount += dto.getChangeAmount();   // 총 변동 금액
//        }
//
//        double totalChangeRate = 0.0;
//        if (totalPrice != 0) {
//            totalChangeRate = (totalChangeAmount / totalPrice) * 100; // 총 변동률 (%)
//        }
//
//        // 4) Response DTO 생성
//        return TodayOfferResponseDTO.builder()
//                .totalPrice(totalPrice)
//                .changeAmount(totalChangeAmount)
//                .changeRate(totalChangeRate)
//                .todayOfferResponseDTOList(todayOfferDTOS)
//                .build();
//    }
}
