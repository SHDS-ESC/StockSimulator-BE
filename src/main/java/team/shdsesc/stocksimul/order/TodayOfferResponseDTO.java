package team.shdsesc.stocksimul.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodayOfferResponseDTO {
    Double totalPrice;
    Double changeAmount;
    Double changeRate;
    List<TodayOfferDTO> todayOfferResponseDTOList;
}
