package team.shdsesc.stocksimul.holdings;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import team.shdsesc.stocksimul.market.entity.Stock;
import team.shdsesc.stocksimul.userprofile.UserProfileRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HoldingsService {
    private final HoldingsRepository holdingsRepository;
    private static final String STOCK_IMG_LINK = "https://financialmodelingprep.com/image-stock/";
    private static final String STOCK_IMG_EXTENSION = ".png";


    HoldingsResponseDTO toHoldingsResponseDTOS(HoldingsEntity holdingsEntity) {
        return HoldingsResponseDTO.builder()
                .ticker(holdingsEntity.getStock().getTicker())
                .name(holdingsEntity.getStock().getName())
                .price(holdingsEntity.getPrice())
                .change(-0.2)
                .changeAmount(-0.48)
                .logo(STOCK_IMG_LINK + holdingsEntity.getStock().getTicker() + STOCK_IMG_EXTENSION)
                .build();

    }

    List<HoldingsResponseDTO> getHoldingsList(Long usersProfileId) {
        List<HoldingsEntity> holdingsEntityList = holdingsRepository.getHoldingsList(usersProfileId);
        return holdingsEntityList
                .stream()
                .map(this::toHoldingsResponseDTOS)
                .toList();
    }

}
