package team.shdsesc.stocksimul.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class JwtToken {
    private String grantType; // JWT에 대한 인증 타입, Bearer 인증 방식 사용할 예정
    private String accessToken;
    private String refreshToken;
}
