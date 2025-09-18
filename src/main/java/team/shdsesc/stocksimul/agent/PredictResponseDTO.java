package team.shdsesc.stocksimul.agent;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@ToString
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
}