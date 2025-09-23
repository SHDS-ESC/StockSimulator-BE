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

    // 미사용: 다종목 범위 조회 메서드는 필요 시 복원

    // 대량 조회: 전체 종목의 특정 기간 종가를 한 번에 조회 (stockId, date, close)
    @Query("select r.id.stockId, r.id.date, r.close from Report r where (:from is null or r.id.date >= :from) and (:to is null or r.id.date <= :to) order by r.id.stockId asc, r.id.date asc")
    List<Object[]> findClosesByRange(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("select r.close from Report r where r.id.stockId = :stockId and  r.id.date = :from")
    Double findClosesPrice(
            @Param("from") LocalDateTime from,
            @Param("stockId") Long stockId
    );

}
