package team.shdsesc.stocksimul.holdings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeDTO {
    private String ticker;           // 종목 코드
    private String name;             // 종목명
    private Double prevPrice;        // 이전 가격
    private Double currentPrice;     // 현재 가격
    private Double changeAmount;     // 변동 금액
    private Double changeRate;       // 변동률 (%)
    private Long quantity;           // 보유 수량
    private String logo;             // 로고 URL
}
