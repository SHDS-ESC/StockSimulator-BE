package team.shdsesc.stocksimul.holdings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioResponseDTO {
    private List<HoldingsDTO> holdingsDTOList;
    private Double totalCurrentPrice;
    private List<ChangeDTO> changeList;
}
