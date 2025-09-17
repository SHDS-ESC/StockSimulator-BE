package team.shdsesc.stocksimul.market.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CandleResponse {

    private String status;
    private List<Long> timestamps;
    private List<String> dates;
    private List<Double> opens;
    private List<Double> highs;
    private List<Double> lows;
    private List<Double> closes;
    private List<Double> volumes;

    public static CandleResponse noData() {
        CandleResponse r = new CandleResponse();
        r.status = "no_data";
        r.timestamps = new ArrayList<>();
        r.dates = new ArrayList<>();
        r.opens = new ArrayList<>();
        r.highs = new ArrayList<>();
        r.lows = new ArrayList<>();
        r.closes = new ArrayList<>();
        r.volumes = new ArrayList<>();
        return r;
    }

}



