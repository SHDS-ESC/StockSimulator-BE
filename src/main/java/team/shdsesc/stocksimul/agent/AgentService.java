package team.shdsesc.stocksimul.agent;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AgentService {

    @Value("${fastApi.url}")
    private String fastApiUrl;

    public Mono<PredictResponseDTO> predict(PredictRequestDTO requestDTO) {
        WebClient webClient = WebClient.builder()
                .baseUrl(fastApiUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        return webClient.post()
                .uri("/predict")
                .bodyValue(requestDTO)
                .retrieve()
                .bodyToMono(PredictResponseDTO.class)
                .onErrorResume(throwable -> {
//                    return Mono.just(new PredictResponseDTO());
                    return Mono.error(throwable);
                });
    }

}
