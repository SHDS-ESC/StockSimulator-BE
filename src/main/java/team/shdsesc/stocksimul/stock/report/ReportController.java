package team.shdsesc.stocksimul.stock.report;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/dev/report")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("{ticker}")
    public List<ReportEntity> getReportByTicker(@PathVariable String ticker) {
        log.info("getReport...");
        return reportService.findByTicker(ticker);
    }

}
