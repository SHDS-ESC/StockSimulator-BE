package team.shdsesc.stocksimul.stock.report;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    @Override
    public List<ReportEntity> findByTicker(String ticker) {
        return List.of();
    }
}
