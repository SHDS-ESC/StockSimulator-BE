package team.shdsesc.stocksimul.userprofile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/userprofile")
@RequiredArgsConstructor
@Log4j2
@Tag(name = "Profile", description = "유저 프로필 관련 API")
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping("/timelines")
    @Operation(summary = "타임라인 목록 조회", description = "타임라인 목록을 조회합니다.")
    public ResponseEntity<List<TimeLineEntity>> getTimeLineList() {
        return ResponseEntity
                .ok()
                .body(userProfileService.getTimeLineList());
    }

    @GetMapping("/profiles/{email}")
    @Operation(summary = "유저 프로필 목록 조회", description = "특정 유저의 프로필 목록을 조회합니다.")
    public ResponseEntity<List<UserProfileDTO>> getUserProfileList(@Parameter(description = "유저 아이디(이메일)") @PathVariable String email) {
        return ResponseEntity
                .ok()
                .body(userProfileService.getUserProfileList(email));
    }

    @GetMapping("/profile/{pid}")
    @Operation(summary = "현재 유저 프로필 조회", description = "특정 유저의 현재 프로필을 조회합니다.")
    public ResponseEntity<UserProfileDTO> getCurrentUserProfile(@Parameter(description = "유저 프로필 아이디") @PathVariable Long pid) {
        return ResponseEntity
                .ok()
                .body(userProfileService.getCurrentUserProfile(pid));
    }

    @PostMapping("/create")
    @Operation(summary = "유저 프로필 생성", description = "유저 프로필을 생성합니다.")
    public ResponseEntity<UserProfileDTO> createTimeLine(@Parameter(description = "유저 타임라인 ID, 닉네임, 이메일") @RequestBody CreateUserProfileDTO createUserProfileDTO) {
        return ResponseEntity
                .ok()
                .body(userProfileService.createUserProfile(createUserProfileDTO));
    }

    @PostMapping("/select")
    @Operation(summary = "프로필 변경 업데이트", description = "유저가 프로필 변경 시 반영합니다.")
    public ResponseEntity<?> selectUserProfile(@Parameter(description = "유저ID, 프로필ID") @RequestBody UpdateUserProfileDTO updateUserProfileDTO) {
        userProfileService.updateUserProfile(updateUserProfileDTO);
        return ResponseEntity
                .ok()
                .build();
    }
}

