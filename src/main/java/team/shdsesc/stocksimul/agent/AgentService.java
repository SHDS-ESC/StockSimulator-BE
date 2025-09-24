package team.shdsesc.stocksimul.agent;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AgentService {

    @Value("${fastApi.url}")
    private String fastApiUrl;

    public Mono<PredictResponseDTO> predict(PredictRequestDTO requestDTO) {
        // HTTP 클라이언트 타임아웃 설정 (5분)
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMinutes(5))  // 응답 타임아웃
                .option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000);  // 연결 타임아웃 30초

        WebClient webClient = WebClient.builder()
                .baseUrl(fastApiUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        return webClient.post()
                .uri("/predict")
                .bodyValue(requestDTO)
                .retrieve()
                .bodyToMono(PredictResponseDTO.class)
                .timeout(Duration.ofMinutes(5))  // Mono 레벨 타임아웃도 5분으로 설정
                .onErrorResume(throwable -> {
//                    return Mono.just(new PredictResponseDTO());
                    return Mono.error(throwable);
                });
    }

}
