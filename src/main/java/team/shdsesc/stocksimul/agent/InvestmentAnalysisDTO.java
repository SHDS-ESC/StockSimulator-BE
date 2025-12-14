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

    private String recommendation;

    private String action;

    private String confidence;

    private Integer score;

    private Integer maxScore;

    private Integer minScore;

    private List<String> signals;

    private MetricsDTO metrics;

    private RiskMetricsDTO riskMetrics;
}
