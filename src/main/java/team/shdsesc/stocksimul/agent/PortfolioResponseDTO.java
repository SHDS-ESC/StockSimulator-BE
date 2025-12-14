package team.shdsesc.stocksimul.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioResponseDTO {
    private List<PortfolioSeries> series;
    private List<PortfolioMetric> metrics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PortfolioSeries {
        private String id;
        private List<DateValue> series;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DateValue {
        private String date;
        private Double value;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PortfolioMetric {
        private String id;
        private QuantstatsMetrics metrics;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuantstatsMetrics {
        private LocalDate startPeriod;
        private LocalDate endPeriod;
        private Double timeInMarket;
        private Double cumulativeReturn;
        private Double cagr;
        private Double sharpe;
        private Double probSharpeRatio;
        private Double sortino;
        private Double omega;
        private Double maxDrawdown;
        private LocalDate maxDdDate;
        private LocalDate maxDdPeriodStart;
        private LocalDate maxDdPeriodEnd;
        private Integer longestDdDays;
        private Double gainPainRatio;
        private Double payoffRatio;
        private Double profitFactor;
        private Double cpcIndex;
        private Double tailRatio;
        private Double outlierWinRatio;
        private Double outlierLossRatio;
        private Double mtd;
        private Double threeM;
        private Double sixM;
        private Double ytd;
        private Double oneY;
        private Double threeYAnn;
        private Double fiveYAnn;
        private Double tenYAnn;
        private Double allTimeAnn;
        private Double avgDrawdown;
        private Integer avgDrawdownDays;
        private Double recoveryFactor;
        private Double ulcerIndex;
        private Double serenityIndex;
        private Double volatilityAnnualized;
    }
}
