package team.shdsesc.stocksimul.agent;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/agent")
@Log4j2
public class AgentController {

    private final AgentService agentService;

    @PostMapping("/predict")
    public Mono<ResponseEntity<PredictResponseDTO>> predict(@RequestBody PredictRequestDTO requestDTO) {
        log.info("Predict request: {}", requestDTO);

        return agentService.predict(requestDTO)
                .map(response -> {
                    log.info("Predict response: {}", response);
                    return ResponseEntity.ok(response);
                })
                .onErrorResume(e -> {
                    log.error("Predict API 오류 - 요청: {}, 오류: {}", requestDTO, e.getMessage(), e);
                    return Mono.error(e);
                });
    }

    @PostMapping("/portfolio/analysis")
    public Mono<ResponseEntity<PortfolioResponseDTO>> getPortfolioAnalysis(@RequestBody PortfolioRequestDTO requestDTO) {
        log.info("Portfolio analysis request: {}", requestDTO);

        return agentService.getPortfolioAnalysis(requestDTO)
                .map(response -> {
                    log.info("Portfolio analysis response: {}", response);
                    return ResponseEntity.ok(response);
                })
                .onErrorResume(e -> {
                    log.error("Portfolio analysis API 오류 - 요청: {}, 오류: {}", requestDTO, e.getMessage(), e);
                    return Mono.error(e);
                });
    }

    @PostMapping("/investment/review")
    public Mono<ResponseEntity<InvestmentReviewResponseDTO>> getInvestmentReview(@RequestBody InvestmentReviewRequestDTO requestDTO) {
        log.info("Investment review request: {}", requestDTO);

        return agentService.getInvestmentReview(requestDTO)
                .map(response -> {
                    log.info("Investment review response generated successfully");
                    return ResponseEntity.ok(response);
                })
                .onErrorResume(e -> {
                    log.error("Investment review API 오류 - 요청: {}, 오류: {}", requestDTO, e.getMessage(), e);
                    return Mono.error(e);
                });
    }

    @PostMapping("/predict-sample")
    public ResponseEntity<PredictResponseDTO> predictSample() {
        // 메트릭 데이터 (전체 필드 포함)
        MetricsDTO metrics = MetricsDTO.builder()
                .currentPrice(229.65)
                .predictedAvgPrice(203.72)
                .predictedMaxPrice(227.30)
                .predictedMinPrice(203.72)
                .expectedTotalReturn(-9.73)
                .expectedAvgDailyReturn(-1.50)
                .predictedVolatility(2.02)
                .upsideProbability(0.0)
                .build();

        // 리스크 메트릭 데이터 (전체 필드 포함)
        RiskMetricsDTO riskMetrics = RiskMetricsDTO.builder()
                .historicalVolatilityAnnualized(42.66)
                .predictedVolatility(2.26)
                .var95(0.0487)
                .maxExpectedLoss(-12.49)
                .maxExpectedGain(0.15)
                .estimatedSharpeRatio(-0.138)
                .build();

        // 투자 분석 데이터 (전체 필드 포함)
        InvestmentAnalysisDTO investmentAnalysis = InvestmentAnalysisDTO.builder()
                .recommendation("매도 고려")
                .action("SELL")
                .confidence("MEDIUM")
                .score(-3)
                .maxScore(5)
                .minScore(-5)
                .signals(Arrays.asList(
                        "매우 낮은 수익률 기대 (-2% 미만)",
                        "매우 낮은 상승 확률 (30% 미만)",
                        "낮은 리스크 (VaR -3% 이상)"
                ))
                .metrics(metrics)
                .riskMetrics(riskMetrics)
                .build();

        return ResponseEntity.ok(PredictResponseDTO.builder()
                .ticker("AAPL")
                .baseDate("2025-09-24")
                .lastPrice(229.65)
                .returnPredictions(Arrays.asList(
                    -1.02,
                    -1.96,
                    -2.54       
                ))
                .pricePredictions(Arrays.asList(
                    227.30,
                    225.15,
                    223.80
                ))
                .predictionDates(Arrays.asList(
                    "2025-09-25",
                    "2025-09-26", 
                    "2025-09-27"
                ))
                .trainDataCount(10030)
                .featureCount(32)
                .investmentAnalysis(investmentAnalysis)
                .chartFull("sample_chart_full_base64_data")
                .chartBrief("sample_chart_brief_base64_data")
                .build());
    }


}
