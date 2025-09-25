package team.shdsesc.stocksimul.order;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team.shdsesc.stocksimul.market.entity.Stock;
import team.shdsesc.stocksimul.userprofile.UserProfileEntity;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodayOfferDTO {
    private OfferType type;
    private Long quantity;
    private Double price;
    private String logoUrl;
    private String name;
    private String ticker;
    private Double change;
    private Double changeAmount;
}
