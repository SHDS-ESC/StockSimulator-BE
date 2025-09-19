package team.shdsesc.stocksimul.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import team.shdsesc.stocksimul.news.NewsEntity;

@Repository
public interface OfferRepository extends JpaRepository<OfferEntity, Long>, OfferRepositoryCustom {

}
