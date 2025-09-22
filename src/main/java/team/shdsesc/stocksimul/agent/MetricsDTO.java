package team.shdsesc.stocksimul.agent;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MetricsDTO {

    @JsonProperty("current_price")
    private double currentPrice;

    @JsonProperty("predicted_avg_price")
    private double predictedAvgPrice;

    @JsonProperty("expected_total_return")
    private double expectedTotalReturn;

    @JsonProperty("upside_probability")
    private double upsideProbability;
}
