package team.shdsesc.stocksimul.userprofile;

import jakarta.persistence.*;
import lombok.*;
import team.shdsesc.stocksimul.auth.util.BaseEntity;
import team.shdsesc.stocksimul.user.UserEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "usersprofile")
public class UserProfileEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long usersProfileId;

    @JoinColumn(name = "users_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity user;

    @JoinColumn(name = "timeline_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private TimeLineEntity timeLine;

    private Double cashBalance;
    private LocalDateTime processDate;
    private String nickname;

    public void setUsersCashBalance(Double cashBalance) {
        this.cashBalance = cashBalance;
    }
}
