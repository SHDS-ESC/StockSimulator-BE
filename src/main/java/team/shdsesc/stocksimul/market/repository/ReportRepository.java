package team.shdsesc.stocksimul.market.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team.shdsesc.stocksimul.market.entity.Report;
import team.shdsesc.stocksimul.market.entity.ReportId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, ReportId> {

    @Query("select max(r.id.date) from Report r where r.id.stockId = :stockId")
    Optional<LocalDateTime> findLastDate(@Param("stockId") Long stockId);

    @Query("select r from Report r where r.id.stockId = :stockId and (:from is null or r.id.date >= :from) and (:to is null or r.id.date <= :to) order by r.id.date asc")
    List<Report> findByStockIdAndRange(
            @Param("stockId") Long stockId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

}
