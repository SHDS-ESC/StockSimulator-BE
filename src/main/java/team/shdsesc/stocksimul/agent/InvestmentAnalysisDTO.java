package team.shdsesc.stocksimul.agent;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InvestmentAnalysisDTO {

    @JsonProperty("recommendation")
    private String recommendation;

    @JsonProperty("action")
    private String action;

    @JsonProperty("confidence")
    private String confidence;

    @JsonProperty("score")
    private Integer score;

    @JsonProperty("max_score")
    private Integer maxScore;

    @JsonProperty("min_score")
    private Integer minScore;

    @JsonProperty("signals")
    private List<String> signals;

    @JsonProperty("metrics")
    private MetricsDTO metrics;

    @JsonProperty("risk_metrics")
    private RiskMetricsDTO riskMetrics;
}
