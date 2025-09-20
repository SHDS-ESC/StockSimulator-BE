package team.shdsesc.stocksimul.agent;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RiskMetricsDTO {

    @JsonProperty("var_95")
    private double var95;

    @JsonProperty("max_expected_loss")
    private double maxExpectedLoss;

    @JsonProperty("estimated_sharpe_ratio")
    private double estimatedSharpeRatio;
}
