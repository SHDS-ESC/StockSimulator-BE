package team.shdsesc.stocksimul.stock;

import java.util.List;

public interface StockService {

    List<StockEntity> findAll();
    StockEntity findByTicker(String ticker);
    List<RealTimeStockDTO> getRealTime(String ticker);
}
