package team.shdsesc.stocksimul.agent;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RiskMetricsDTO {

    @JsonProperty("historical_volatility_annualized")
    private Double historicalVolatilityAnnualized;

    @JsonProperty("predicted_volatility")
    private Double predictedVolatility;

    @JsonProperty("var_95")
    private Double var95;

    @JsonProperty("max_expected_loss")
    private Double maxExpectedLoss;

    @JsonProperty("max_expected_gain")
    private Double maxExpectedGain;

    @JsonProperty("estimated_sharpe_ratio")
    private Double estimatedSharpeRatio;
}
