package team.shdsesc.stocksimul.stock;

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
//@RequestMapping("/api/stock")
@RequestMapping("/dev/stock")
public class StockController {

    private final StockService stockService;

    @GetMapping("/list")
    public List<StockEntity> getList() {
        log.info("getList....");
        List<StockEntity> list = stockService.findAll();
        return list;
    }

    @GetMapping("/{ticker}")
    public StockEntity getStockByTicker(@PathVariable String ticker) {
        log.info("getStockByTicker...");
        return stockService.findByTicker(ticker);
    }

    @GetMapping("/real-time/{ticker}")
    public List<RealTimeStockDTO> getRealTime(@PathVariable String ticker) {
        log.info("getRealTime... : " +  ticker);
        return stockService.getRealTime(ticker);
    }


}
