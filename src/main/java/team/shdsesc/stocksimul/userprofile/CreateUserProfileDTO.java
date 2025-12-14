package team.shdsesc.stocksimul.userprofile;

import lombok.*;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserProfileDTO {
    private Long timelineId;
    private String email;
    private String nickname;
}
