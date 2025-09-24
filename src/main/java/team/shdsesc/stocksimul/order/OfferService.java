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

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class OfferService {
    private final OfferRepository offerRepository;
    private final UserProfileRepository userProfileRepository;
    private final MarketStockRepository marketStockRepository;
    private final HoldingsRepository holdingsRepository;

    OfferEntity toOfferEntity(OfferRequestDTO dto) {
        // 주문에 넣을 유저 프로필 가져오기
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
        Double price = holding.getTotalPrice();//처음이면 0.0, 아니면 기존 평단가

        // 수량 계산 (BUY는 +, SELL은 -)
        long quantity = dto.type == OfferType.BUY
                ? amount + dto.quantity // 기존 갯수에 구매 갯수 추가
                : amount - dto.quantity;// 기존 갯수에 구매 갯수 감소

        // 실 구매 가격
        double tPrice = dto.price * dto.quantity;

        // 보유자산 총합
        double totalPrice = dto.type == OfferType.BUY
                ? price + tPrice // 기존 총합 금액 + 토탈 금액
                : price - tPrice;// 기촌 총합 금액 - 토탈 금액

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
                .build();
    }

    void calcOfferStock(OfferRequestDTO offerRequestDTO) {
        OfferEntity offerEntity = toOfferEntity(offerRequestDTO);
        offerRepository.save(offerEntity);
    }

    public List<OfferResponseDTO> getOfferHistory(Long usersProfileId) {
        List<OfferEntity> entities = offerRepository.findByUserProfile_UsersProfileIdOrderByOfferDateDesc(usersProfileId);

        return entities.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    private OfferResponseDTO toResponseDTO(OfferEntity offerEntity) {
        Stock stock = marketStockRepository
                .findById(offerEntity.getStock().getStockId())
                .orElse(offerEntity.getStock());

        return OfferResponseDTO.builder()
                .offerId(offerEntity.getOfferId())
                .offerDate(offerEntity.getOfferDate().toLocalDate())
                .usersProfileId(offerEntity.getUserProfile().getUsersProfileId())
                .stock(stock.getTicker())
                .type(offerEntity.getType())
                .quantity(offerEntity.getQuantity())
                .price(offerEntity.getPrice())
                .build();

    }
}
