package team.shdsesc.stocksimul.order;

import jakarta.persistence.*;
import lombok.*;
import team.shdsesc.stocksimul.market.entity.Stock;
import team.shdsesc.stocksimul.userprofile.UserProfileEntity;
import java.time.LocalDateTime;

@Entity
@Table(name = "offer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class OfferEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "offer_id")
    private Long offerId;

    @Column(name = "offer_date", nullable = false)
    private LocalDateTime offerDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_profile_id", nullable = false)
    private UserProfileEntity userProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 10)
    private OfferType type;

    @Column(name = "quantity", nullable = false)
    private Long quantity;
}

