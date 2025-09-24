package team.shdsesc.stocksimul.order;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferResponseDTO {
    private Long offerId;
    private LocalDate offerDate;
    private Long usersProfileId;
    private String stock;
    private OfferType type;
    private Long quantity;
    private Double price;

}
