package team.shdsesc.stocksimul.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioRequestDTO {
    private String startDate;
    private String endDate;
    private Double baseValue;
    private String rebalance;
    private List<Portfolio> portfolios;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Portfolio {
        private String id;
        private List<String> tickers;
        private List<Double> weights;
    }
}
