package team.shdsesc.stocksimul.agent;


import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;


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
                .doOnError(error -> log.error("Predict 처리 중 오류 발생: {}", error.getMessage(), error))
                .onErrorResume(error -> {
                    log.error("Predict API 오류 - 요청: {}, 오류: {}", requestDTO, error.getMessage(), error);
                    // 글로벌 예외 처리기가 처리하도록 예외를 다시 던짐
                    return Mono.error(error);
                });
    }

}
