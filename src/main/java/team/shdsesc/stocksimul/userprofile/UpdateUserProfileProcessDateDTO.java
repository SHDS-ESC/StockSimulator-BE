package team.shdsesc.stocksimul.userprofile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserProfileProcessDateDTO {
    private Long userProfileId;
    private String prevProcessDate;
    private String processDate;
}
