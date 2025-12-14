package team.shdsesc.stocksimul.user;

import jakarta.persistence.*;
import lombok.*;
import team.shdsesc.stocksimul.userprofile.UserProfileEntity;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "userslikes")
public class UsersLikesEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "users_likes_id")
    private Long usersLikesId;

    @JoinColumn(name = "users_profile_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private UserProfileEntity userProfile;

    @Column(name = "stock_id", nullable = false)
    private Long stockId;
}

