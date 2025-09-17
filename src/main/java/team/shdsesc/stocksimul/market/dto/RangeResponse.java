package team.shdsesc.stocksimul.market.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RangeResponse {
    private String status;
    private long last;
    private long from;
    private long to;

    public static RangeResponse noData() {
        RangeResponse r = new RangeResponse();
        r.status = "no_data";
        return r;
    }

}



