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

    @JsonProperty("base_date")
    private String baseDate;

    // Chat.jsx에서 시뮬레이션 결과에 사용됨
    @JsonProperty("last_price")
    private Double lastPrice;

    @JsonProperty("train_data_count")
    private Integer trainDataCount;

    @JsonProperty("feature_count")
    private Integer featureCount;

    @JsonProperty("investment_analysis")
    private InvestmentAnalysisDTO investmentAnalysis;

    // 차트 이미지 base64
    @JsonProperty("chart_full")
    private String chartFull;

    @JsonProperty("chart_brief")
    private String chartBrief;

    // 차트용 예측 데이터 (Chat.jsx 모달에서 사용)
    @JsonProperty("prediction_dates")
    private java.util.List<String> predictionDates;

    @JsonProperty("price_predictions")
    private java.util.List<Double> pricePredictions;

    @JsonProperty("return_predictions")
    private java.util.List<Double> returnPredictions;

    // --- Front-ready chart series ---
    @JsonProperty("historical")
    private java.util.List<ChartSeriesPoint> historical;

    @JsonProperty("predictions")
    private java.util.List<ChartSeriesPoint> predictions;

    @JsonProperty("pred_end_date")
    private String predEndDate;

}