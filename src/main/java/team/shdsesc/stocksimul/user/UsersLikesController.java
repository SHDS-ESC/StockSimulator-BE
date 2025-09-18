package team.shdsesc.stocksimul.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import team.shdsesc.stocksimul.userprofile.UserProfileRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/likes")
@RequiredArgsConstructor
@Log4j2
public class UsersLikesController {
    
    private final UsersLikesService usersLikesService;
    private final UserProfileRepository userProfileRepository;
    
    // 관심종목 추가
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addToWatchlist(
            @RequestParam("profileId") Long profileId,
            @RequestParam("stockId") String ticker) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean success = usersLikesService.addToWatchlist(profileId, ticker);
            
            if (success) {
                response.put("status", "success");
                response.put("message", "관심종목에 추가되었습니다");
            } else {
                response.put("status", "error");
                response.put("message", "이미 관심종목에 있거나 추가에 실패했습니다");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("관심종목 추가 API 오류: {}", e.getMessage());
            response.put("status", "error");
            response.put("message", "관심종목 추가 중 오류가 발생했습니다");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    // 관심종목 제거
    @DeleteMapping("/remove")
    public ResponseEntity<Map<String, Object>> removeFromWatchlist(
            @RequestParam("profileId") Long profileId,
            @RequestParam("stockId") String ticker) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean success = usersLikesService.removeFromWatchlist(profileId, ticker);
            
            if (success) {
                response.put("status", "success");
                response.put("message", "관심종목에서 제거되었습니다");
            } else {
                response.put("status", "error");
                response.put("message", "관심종목 제거에 실패했습니다");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("관심종목 제거 API 오류: {}", e.getMessage());
            response.put("status", "error");
            response.put("message", "관심종목 제거 중 오류가 발생했습니다");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    // 관심종목 목록 조회
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getWatchlist(
            @RequestParam("profileId") Long profileId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 프로필 존재 확인
            if (!userProfileRepository.existsById(profileId)) {
                response.put("status", "error");
                response.put("message", "프로필을 찾을 수 없습니다");
                return ResponseEntity.badRequest().body(response);
            }
            
            List<String> watchlist = usersLikesService.getWatchlist(profileId);
            
            response.put("status", "success");
            response.put("watchlist", watchlist);
            response.put("count", watchlist.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("관심종목 목록 조회 API 오류: {}", e.getMessage());
            response.put("status", "error");
            response.put("message", "관심종목 목록 조회 중 오류가 발생했습니다");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    // 관심종목 여부 확인
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkWatchlist(
            @RequestParam("profileId") Long profileId,
            @RequestParam("stockId") String ticker) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean isInWatchlist = usersLikesService.isInWatchlist(profileId, ticker);
            
            response.put("status", "success");
            response.put("isInWatchlist", isInWatchlist);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("관심종목 확인 API 오류: {}", e.getMessage());
            response.put("status", "error");
            response.put("message", "관심종목 확인 중 오류가 발생했습니다");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    // 관심종목 토글 (추가/제거)
    @PostMapping("/toggle")
    public ResponseEntity<Map<String, Object>> toggleWatchlist(
            @RequestParam("profileId") Long profileId,
            @RequestParam("stockId") String ticker) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean isInWatchlist = usersLikesService.isInWatchlist(profileId, ticker);
            boolean success;
            String action;
            
            if (isInWatchlist) {
                success = usersLikesService.removeFromWatchlist(profileId, ticker);
                action = "제거";
            } else {
                success = usersLikesService.addToWatchlist(profileId, ticker);
                action = "추가";
            }
            
            if (success) {
                response.put("status", "success");
                response.put("message", "관심종목에서 " + action + "되었습니다");
                response.put("isInWatchlist", !isInWatchlist);
            } else {
                response.put("status", "error");
                response.put("message", "관심종목 " + action + "에 실패했습니다");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("관심종목 토글 API 오류: {}", e.getMessage());
            response.put("status", "error");
            response.put("message", "관심종목 토글 중 오류가 발생했습니다");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}

