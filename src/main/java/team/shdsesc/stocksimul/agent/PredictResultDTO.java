package team.shdsesc.stocksimul.agent;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PredictResultDTO {

    @JsonProperty("day")
    private int day;

    @JsonProperty("return_rate")
    private double returnRate;

    @JsonProperty("price")
    private double predictPrice;
}