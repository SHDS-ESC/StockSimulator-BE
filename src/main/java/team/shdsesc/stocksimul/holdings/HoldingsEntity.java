package team.shdsesc.stocksimul.holdings;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import team.shdsesc.stocksimul.market.entity.Stock;
import team.shdsesc.stocksimul.userprofile.UserProfileEntity;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "holdings")
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HoldingsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "holdings_id")
    private Long holdingsId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_profile_id", nullable = false)
    private UserProfileEntity userProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(name = "quantity")
    private Long quantity;

    @Column(name = "price")
    private Double price;

    @LastModifiedDate
    @Column(name = "mod_date")
    private LocalDateTime modDate;
}
