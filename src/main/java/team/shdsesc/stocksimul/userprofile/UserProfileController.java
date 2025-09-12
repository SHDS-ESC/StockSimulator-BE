package team.shdsesc.stocksimul.userprofile;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/userprofile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping("/timelines")
    public ResponseEntity<List<TimeLineEntity>> getTimeLineList() {
        return userProfileService.getTimeLineList();
    }

    @PostMapping("/create")
    public ResponseEntity<UserProfileEntity> createTimeLine(@RequestBody UserProfileDTO userProfileDTO) {
        return userProfileService.createUserProfile(userProfileDTO);
    }
}
