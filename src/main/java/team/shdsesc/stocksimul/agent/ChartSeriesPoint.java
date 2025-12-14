package team.shdsesc.stocksimul.agent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChartSeriesPoint {
    private long time;   // epoch seconds (UTC)
    private double value;
}


