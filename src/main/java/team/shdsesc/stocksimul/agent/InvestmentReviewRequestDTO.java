package team.shdsesc.stocksimul.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestmentReviewRequestDTO {
    private PredictRequestDTO predictRequest;
    private PortfolioRequestDTO portfolioRequest;
}
