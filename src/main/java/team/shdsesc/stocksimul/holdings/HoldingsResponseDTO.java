package team.shdsesc.stocksimul.holdings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoldingsResponseDTO {
    private String ticker;
    private String name;
    private Double price;
    private Double change;
    private Double changeAmount;
    private String logo;
}
