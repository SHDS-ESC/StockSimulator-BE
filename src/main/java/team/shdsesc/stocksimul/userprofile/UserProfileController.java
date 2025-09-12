package team.shdsesc.stocksimul.userprofile;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import team.shdsesc.stocksimul.redis.dao.RedisDAO;

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
    public ResponseEntity<UserProfileEntity> createTimeLine(@RequestBody CreateUserProfileDTO createUserProfileDTO) {
        return userProfileService.createUserProfile(createUserProfileDTO);
    }

    @GetMapping("/profiles/{email}")
    public ResponseEntity<List<UserProfileDTO>> getUserProfileList(@PathVariable String email) {
        return userProfileService.getUserProfileList(email);
    }
}
