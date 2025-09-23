package team.shdsesc.stocksimul.agent;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dev/agent")
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

    @PostMapping("/predict-sample")
    public ResponseEntity<PredictResponseDTO> predictSample() {
        // 메트릭 데이터 (기존 필드만 사용)
        MetricsDTO metrics = MetricsDTO.builder()
                .currentPrice(229.65)
                .predictedAvgPrice(203.72)
                .expectedTotalReturn(-9.73)
                .upsideProbability(0.0)
                .build();

        // 리스크 메트릭 데이터 (기존 필드만 사용)
        RiskMetricsDTO riskMetrics = RiskMetricsDTO.builder()
                .var95(0.0487)
                .maxExpectedLoss(-12.49)
                .estimatedSharpeRatio(-0.138)
                .build();

        // 투자 분석 데이터 (기존 필드만 사용)
        InvestmentAnalysisDTO investmentAnalysis = InvestmentAnalysisDTO.builder()
                .recommendation("매도 고려")
                .action("SELL")
                .confidence("MEDIUM")
                .score(-3)
                .maxScore(5)
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
                .chartFull("sample_chart_full_base64_data")
                .chart30d("sample_chart_30d_base64_data")
                .investmentAnalysis(investmentAnalysis)
                .build());
    }

}
