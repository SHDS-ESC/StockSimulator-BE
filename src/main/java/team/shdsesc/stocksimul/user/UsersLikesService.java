package team.shdsesc.stocksimul.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.shdsesc.stocksimul.userprofile.UserProfileEntity;
import team.shdsesc.stocksimul.market.entity.Stock;
import team.shdsesc.stocksimul.market.repository.MarketStockRepository;
import team.shdsesc.stocksimul.userprofile.UserProfileRepository;

import java.util.List;
import java.util.Optional;

@Log4j2
@Service
@RequiredArgsConstructor
public class UsersLikesService {
    
    private final UsersLikesRepository usersLikesRepository;
    private final UserProfileRepository userProfileRepository;
    private final MarketStockRepository marketStockRepository;
    
    // 관심종목 추가
    @Transactional
    public boolean addToWatchlist(Long profileId, String ticker) {
        try {
            // 프로필 존재 확인
            if (!userProfileRepository.existsById(profileId)) {
                log.warn("프로필이 존재하지 않습니다: {}", profileId);
                return false;
            }
            
            // 티커로 stock_id 조회
            Optional<Stock> stockOpt = marketStockRepository.findByTicker(ticker);
            if (stockOpt.isEmpty()) {
                log.warn("티커를 찾을 수 없습니다: {}", ticker);
                return false;
            }
            Long stockId = stockOpt.get().getStockId();

            // 이미 관심종목에 있는지 확인
            Optional<UsersLikesEntity> existing = usersLikesRepository
                .findByUserProfileIdAndStockId(profileId, stockId);
            
            if (existing.isPresent()) {
                log.info("이미 관심종목에 있습니다: profileId={}, stockId={}", profileId, stockId);
                return false;
            }
            
            // 관심종목 추가
            UserProfileEntity userProfile = userProfileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("프로필을 찾을 수 없습니다"));
            
            UsersLikesEntity usersLikes = UsersLikesEntity.builder()
                .userProfile(userProfile)
                .stockId(stockId)
                .build();
            
            usersLikesRepository.save(usersLikes);
            log.info("관심종목 추가 성공: profileId={}, stockId={}", profileId, stockId);
            return true;
            
        } catch (Exception e) {
            log.error("관심종목 추가 실패: profileId={}, ticker={}, error={}", profileId, ticker, e.getMessage());
            return false;
        }
    }
    
    // 관심종목 제거
    @Transactional
    public boolean removeFromWatchlist(Long profileId, String ticker) {
        try {
            Optional<Stock> stockOpt = marketStockRepository.findByTicker(ticker);
            if (stockOpt.isEmpty()) {
                log.warn("티커를 찾을 수 없습니다: {}", ticker);
                return false;
            }
            Long stockId = stockOpt.get().getStockId();

            int deletedCount = usersLikesRepository.deleteByUserProfileIdAndStockId(profileId, stockId);
            if (deletedCount > 0) {
                log.info("관심종목 제거 성공: profileId={}, stockId={}", profileId, stockId);
                return true;
            } else {
                log.warn("관심종목이 존재하지 않습니다: profileId={}, stockId={}", profileId, stockId);
                return false;
            }
        } catch (Exception e) {
            log.error("관심종목 제거 실패: profileId={}, ticker={}, error={}", profileId, ticker, e.getMessage());
            return false;
        }
    }
    
    // 관심종목 목록 조회
    public List<String> getWatchlist(Long profileId) {
        try {
            List<UsersLikesEntity> likes = usersLikesRepository.findByUserProfileId(profileId);
            // stock_id -> ticker 변환
            List<Long> stockIds = likes.stream().map(UsersLikesEntity::getStockId).toList();
            if (stockIds.isEmpty()) return List.of();
            // 간단히 one-by-one 조회 (필요시 batch 최적화 가능)
            return stockIds.stream()
                    .map(id -> marketStockRepository.findById(id).map(Stock::getTicker).orElse(null))
                    .filter(t -> t != null)
                    .toList();
        } catch (Exception e) {
            log.error("관심종목 목록 조회 실패: profileId={}, error={}", profileId, e.getMessage());
            return List.of();
        }
    }
    
    // 관심종목 여부 확인
    public boolean isInWatchlist(Long profileId, String ticker) {
        try {
            Optional<Stock> stockOpt = marketStockRepository.findByTicker(ticker);
            if (stockOpt.isEmpty()) {
                return false;
            }
            Long stockId = stockOpt.get().getStockId();
            return usersLikesRepository.findByUserProfileIdAndStockId(profileId, stockId).isPresent();
        } catch (Exception e) {
            log.error("관심종목 확인 실패: profileId={}, ticker={}, error={}", profileId, ticker, e.getMessage());
            return false;
        }
    }
    
    // 프로필의 모든 관심종목 삭제
    @Transactional
    public boolean clearWatchlist(Long profileId) {
        try {
            int deletedCount = usersLikesRepository.deleteByUserProfileId(profileId);
            log.info("관심종목 전체 삭제 성공: profileId={}, deletedCount={}", profileId, deletedCount);
            return true;
        } catch (Exception e) {
            log.error("관심종목 전체 삭제 실패: profileId={}, error={}", profileId, e.getMessage());
            return false;
        }
    }
}
