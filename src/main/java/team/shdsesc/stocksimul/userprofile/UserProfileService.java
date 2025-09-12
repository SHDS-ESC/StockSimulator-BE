package team.shdsesc.stocksimul.userprofile;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import team.shdsesc.stocksimul.user.UserEntity;
import team.shdsesc.stocksimul.user.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserProfileService {
    private final TimeLineRepository timeLineRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    public ResponseEntity<List<TimeLineEntity>> getTimeLineList() {
        return ResponseEntity
                .ok()
                .body(timeLineRepository.findAll());
    }

    public ResponseEntity<UserProfileEntity> createUserProfile(UserProfileDTO userProfileDTO) {
        UserEntity user = userRepository.findUserWithRolesByUserId(userProfileDTO.getEmail()).orElseThrow(() -> new RuntimeException("User not found"));
        TimeLineEntity timeLine = timeLineRepository.findById(userProfileDTO.getTimelineId()).orElseThrow(() -> new RuntimeException("Timeline not found"));
        UserProfileEntity userProfileEntity = UserProfileEntity.toUserProfileEntity(userProfileDTO, user, timeLine);
        userProfileRepository.save(userProfileEntity);

        return ResponseEntity
                .ok()
                .body(userProfileEntity);
    }
}
