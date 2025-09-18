package team.shdsesc.stocksimul.agent;


import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

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
                });
    }

}
