package team.shdsesc.stocksimul.agent;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class InvestmentAnalysisDTO {

    @JsonProperty("recommendation")
    private String recommendation;

    @JsonProperty("action")
    private String action;

    @JsonProperty("confidence")
    private String confidence;

    @JsonProperty("score")
    private int score;

    @JsonProperty("max_score")
    private int maxScore;

    @JsonProperty("signals")
    private List<String> signals;

    @JsonProperty("metrics")
    private MetricsDTO metrics;

    @JsonProperty("risk_metrics")
    private RiskMetricsDTO riskMetrics;
}
