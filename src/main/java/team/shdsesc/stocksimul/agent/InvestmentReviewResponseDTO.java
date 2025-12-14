package team.shdsesc.stocksimul.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestmentReviewResponseDTO {
    private String review;  // LLM이 생성한 자연어 투자 점검 결과
    private PredictResponseDTO predictResponse;
    private PortfolioResponseDTO portfolioResponse;
}
