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

    @JsonProperty("expected_total_return")
    private Double expectedTotalReturn;

    @JsonProperty("upside_probability")
    private Double upsideProbability;
}
