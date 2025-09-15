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
        return ResponseEntity
                .ok()
                .body(userProfileService.getTimeLineList());
    }

    @GetMapping("/profiles/{email}")
    public ResponseEntity<List<UserProfileDTO>> getUserProfileList(@PathVariable String email) {
        return ResponseEntity
                .ok()
                .body(userProfileService.getUserProfileList(email));
    }

    @PostMapping("/create")
    public ResponseEntity<UserProfileEntity> createTimeLine(@RequestBody CreateUserProfileDTO createUserProfileDTO) {
        return ResponseEntity
                .ok()
                .body(userProfileService.createUserProfile(createUserProfileDTO));
    }

    @PostMapping("/select")
    public ResponseEntity<?> selectUserProfile(@RequestBody UpdateUserProfileDTO updateUserProfileDTO) {
        userProfileService.updateUserProfile(updateUserProfileDTO);
        return ResponseEntity
                .ok()
                .build();
    }
}

