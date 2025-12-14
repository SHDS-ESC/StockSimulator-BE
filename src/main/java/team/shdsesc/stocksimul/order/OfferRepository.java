package team.shdsesc.stocksimul.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import team.shdsesc.stocksimul.news.NewsEntity;
import java.util.List;

@Repository
public interface OfferRepository extends JpaRepository<OfferEntity, Long>, OfferRepositoryCustom {


    // OfferRepository.java에 추가
    @Query("SELECT o FROM OfferEntity o JOIN FETCH o.stock JOIN FETCH o.userProfile WHERE o.userProfile.usersProfileId = :usersProfileId ORDER BY o.offerDate DESC")
    List<OfferEntity> findByUserProfile_UsersProfileIdOrderByOfferDateDesc(@Param("usersProfileId") Long usersProfileId);
}
