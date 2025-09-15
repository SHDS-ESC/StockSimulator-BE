package team.shdsesc.stocksimul.market.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import team.shdsesc.stocksimul.market.entity.SnpHistory;

import java.util.List;

public interface SnpHistoryRepository extends JpaRepository<SnpHistory, Long> {

    interface SymbolView {
        String getTicker();
        String getName();
        String getSector();
        String getIndustry();
    }

    @Query("select sh.ticker as ticker, max(sh.name) as name, max(sh.sector) as sector, max(sh.industry) as industry from SnpHistory sh group by sh.ticker order by sh.ticker asc")
    List<SymbolView> findAllSymbols();
}



