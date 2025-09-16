package team.shdsesc.stocksimul.stock.report;

import java.util.List;

public interface ReportService {

    List<ReportEntity> findByTicker(String ticker);


}
