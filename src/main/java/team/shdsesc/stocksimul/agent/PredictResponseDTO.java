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

    // Chat.jsx에서 시뮬레이션 결과에 사용됨
    @JsonProperty("last_price")
    private Double lastPrice;

    // 차트용 예측 데이터 (Chat.jsx 모달에서 사용)
    @JsonProperty("prediction_dates")
    private java.util.List<String> predictionDates;

    @JsonProperty("price_predictions")
    private java.util.List<Double> pricePredictions;

    @JsonProperty("return_predictions")
    private java.util.List<Double> returnPredictions;

}