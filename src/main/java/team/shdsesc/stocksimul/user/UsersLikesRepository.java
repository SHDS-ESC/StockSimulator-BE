package team.shdsesc.stocksimul.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsersLikesRepository extends JpaRepository<UsersLikesEntity, Long> {
    
    // 특정 프로필의 관심종목 목록 조회
    @Query("SELECT ul FROM UsersLikesEntity ul WHERE ul.userProfile.usersProfileId = :profileId")
    List<UsersLikesEntity> findByUserProfileId(@Param("profileId") Long profileId);
    
    // 특정 프로필의 특정 주식이 관심종목에 있는지 확인
    @Query("SELECT ul FROM UsersLikesEntity ul WHERE ul.userProfile.usersProfileId = :profileId AND ul.stockId = :stockId")
    Optional<UsersLikesEntity> findByUserProfileIdAndStockId(@Param("profileId") Long profileId, @Param("stockId") Long stockId);
    
    // 특정 프로필의 관심종목 삭제
    @Modifying
    @Query("DELETE FROM UsersLikesEntity ul WHERE ul.userProfile.usersProfileId = :profileId AND ul.stockId = :stockId")
    int deleteByUserProfileIdAndStockId(@Param("profileId") Long profileId, @Param("stockId") Long stockId);
    
    // 특정 프로필의 모든 관심종목 삭제
    @Modifying
    @Query("DELETE FROM UsersLikesEntity ul WHERE ul.userProfile.usersProfileId = :profileId")
    int deleteByUserProfileId(@Param("profileId") Long profileId);
}
