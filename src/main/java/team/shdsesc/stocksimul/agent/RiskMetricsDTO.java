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

    @JsonProperty("var_95")
    private Double var95;

    @JsonProperty("max_expected_loss")
    private Double maxExpectedLoss;

    @JsonProperty("estimated_sharpe_ratio")
    private Double estimatedSharpeRatio;
}
