package team.shdsesc.stocksimul.agent;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@ToString(exclude = {"chartFull", "chart30d"})
public class PredictResponseDTO {

    @JsonProperty("ticker")
    private String ticker;

    @JsonProperty("base_date")
    private LocalDate baseDate;

    @JsonProperty("last_price")
    private Double lastPrice;

    @JsonProperty("train_data_count")
    private int trainDataCount;

    @JsonProperty("feature_count")
    private int featureCount;

    @JsonProperty("predicted")
    private List<PredictResultDTO> results;

    // 차트 이미지 base64
    @JsonProperty("chart_full")
    private String chartFull;

    @JsonProperty("chart_30d")
    private String chart30d;


}