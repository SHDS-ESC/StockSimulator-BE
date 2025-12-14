package team.shdsesc.stocksimul.userprofile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimeLineRepository extends JpaRepository<TimeLineEntity, Long> {
}
