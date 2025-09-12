package team.shdsesc.stocksimul.userprofile;

import lombok.*;
import team.shdsesc.stocksimul.user.UserEntity;

import java.time.LocalDateTime;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDTO {
    private Long timelineId;
    private String email;
    private String nickname;
}
