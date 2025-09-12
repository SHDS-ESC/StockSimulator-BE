package team.shdsesc.stocksimul.news;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface NewsRepository extends JpaRepository<NewsEntity, Long>, QuerydslPredicateExecutor<NewsEntity> {
    
}
