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

    private String ticker;

    private String baseDate;

    // Chat.jsx에서 시뮬레이션 결과에 사용됨
    private Double lastPrice;

    private Integer trainDataCount;

    private Integer featureCount;

    private InvestmentAnalysisDTO investmentAnalysis;

    // 차트 이미지 base64
    private String chartFull;

    private String chartBrief;

    // 차트용 예측 데이터 (Chat.jsx 모달에서 사용)
    private java.util.List<String> predictionDates;

    private java.util.List<Double> pricePredictions;

    private java.util.List<Double> returnPredictions;

    // --- Front-ready chart series ---
    private java.util.List<ChartSeriesPoint> historical;

    private java.util.List<ChartSeriesPoint> predictions;

    private String predEndDate;

}