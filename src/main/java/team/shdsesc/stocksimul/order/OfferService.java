package team.shdsesc.stocksimul.order;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import team.shdsesc.stocksimul.holdings.HoldingsEntity;
import team.shdsesc.stocksimul.holdings.HoldingsRepository;
import team.shdsesc.stocksimul.market.entity.Stock;
import team.shdsesc.stocksimul.market.repository.MarketStockRepository;
import team.shdsesc.stocksimul.userprofile.UserProfileEntity;
import team.shdsesc.stocksimul.userprofile.UserProfileRepository;

import java.sql.Date;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
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
                .orElseGet(() -> {
                    HoldingsEntity newHolding = HoldingsEntity.builder()
                            .userProfile(userProfile)
                            .stock(stock)
                            .price(dto.price)
                            .modDate(LocalDateTime.now())
                            .quantity(0L)
                            .build();
                    return holdingsRepository.save(newHolding);
                });
        Long amount = holding.getQuantity();

        // 수량 계산 (BUY는 +, SELL은 -)
        long quantity = dto.type == OfferType.BUY
                ? amount + dto.quantity
                : amount - dto.quantity;

        // 수량이 0보다 작아지면 실패
        if (quantity < 0) {
            throw new RuntimeException("quantity less than 0");
        }

        // 0보다 크면 업데이트
        holdingsRepository.updateHoldingStockAmount(dto.usersProfileId, stock.getStockId(), quantity);
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
}
