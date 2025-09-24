package team.shdsesc.stocksimul.agent;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MetricsDTO {

    @JsonProperty("current_price")
    private Double currentPrice;

    @JsonProperty("predicted_avg_price")
    private Double predictedAvgPrice;

    @JsonProperty("predicted_max_price")
    private Double predictedMaxPrice;

    @JsonProperty("predicted_min_price")
    private Double predictedMinPrice;

    @JsonProperty("expected_total_return")
    private Double expectedTotalReturn;

    @JsonProperty("expected_avg_daily_return")
    private Double expectedAvgDailyReturn;

    @JsonProperty("predicted_volatility")
    private Double predictedVolatility;

    @JsonProperty("upside_probability")
    private Double upsideProbability;
}
