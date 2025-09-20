package team.shdsesc.stocksimul.agent;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(exclude = {"chartFull", "chart30d"})
public class PredictResponseDTO {

    @JsonProperty("ticker")
    private String ticker;

    // 차트 이미지 base64
    @JsonProperty("chart_full")
    private String chartFull;

    @JsonProperty("chart_30d")
    private String chart30d;

    @JsonProperty("investment_analysis")
    private InvestmentAnalysisDTO investmentAnalysis;

}