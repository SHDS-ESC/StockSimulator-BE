package team.shdsesc.stocksimul.agent;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@ToString
public class PredictRequestDTO {

    String ticker;

    @JsonProperty("today")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate baseDate;

    int trainDays;

    int predictSteps;

}
