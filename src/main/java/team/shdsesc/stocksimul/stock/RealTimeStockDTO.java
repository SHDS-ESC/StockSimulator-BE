package team.shdsesc.stocksimul.stock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RealTimeStockDTO {
    private String symbol;
    private String name;
    private String price;
    private String change;
    private String changePercent;
    private String volume;
    private String logo;
}
