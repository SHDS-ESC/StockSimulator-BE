package team.shdsesc.stocksimul.market.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import team.shdsesc.stocksimul.market.entity.StockInfo;

import java.util.List;

public interface StockInfoRepository extends JpaRepository<StockInfo, String> {

    interface InfoView {
        String getTicker();
        String getSecurity();
        String getSector();
        String getIndustry();
    }

    @Query("select si.ticker as ticker, si.security as security, si.sector as sector, si.industry as industry from StockInfo si order by si.ticker asc")
    List<InfoView> findAllInfo();
}



