package team.shdsesc.stocksimul.agent;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@ToString(exclude = {"chartFull", "chartBrief"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PredictResponseDTO {

    @JsonProperty("ticker")
    private String ticker;

    // 차트 이미지 base64
    @JsonProperty("chart_full")
    private String chartFull;

    @JsonProperty("chart_brief")
    private String chartBrief;

    @JsonProperty("investment_analysis")
    private InvestmentAnalysisDTO investmentAnalysis;

}