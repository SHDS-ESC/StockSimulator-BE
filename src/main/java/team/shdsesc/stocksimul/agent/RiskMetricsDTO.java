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

    private Double historicalVolatilityAnnualized;

    private Double predictedVolatility;

    private Double var95;

    private Double maxExpectedLoss;

    private Double maxExpectedGain;

    private Double estimatedSharpeRatio;
}
