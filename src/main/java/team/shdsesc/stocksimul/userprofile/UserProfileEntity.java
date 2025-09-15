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
@Table(name = "UserProfile")
public class UserProfileEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userProfileId;

    @JoinColumn(name = "user_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity user;

    @JoinColumn(name = "timeline_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private TimeLineEntity timeLine;

    private Long cashBalance;
    private LocalDateTime processDate;
    private String nickname;
}
