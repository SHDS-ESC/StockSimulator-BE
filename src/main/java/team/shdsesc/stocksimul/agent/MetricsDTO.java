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

    private Double currentPrice;

    private Double predictedAvgPrice;

    private Double predictedMaxPrice;

    private Double predictedMinPrice;

    private Double expectedTotalReturn;

    private Double expectedAvgDailyReturn;

    private Double predictedVolatility;

    private Double upsideProbability;
}
