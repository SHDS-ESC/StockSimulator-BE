package team.shdsesc.stocksimul.stock;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<StockEntity>> getList() {
        log.info("getList....");
        List<StockEntity> list = stockService.findAll();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{ticker}")
    public ResponseEntity<StockEntity> getStockByTicker(@PathVariable String ticker) {
        log.info("getStockByTicker...");
        StockEntity stock = stockService.findByTicker(ticker);
        return ResponseEntity.ok(stock);
    }

    @GetMapping("/real-time/{ticker}")
    public ResponseEntity<List<RealTimeStockDTO>> getRealTime(@PathVariable String ticker) {
        log.info("getRealTime... : " +  ticker);
        List<RealTimeStockDTO> realTimeStockDTOList = stockService.getRealTime(ticker);
        return ResponseEntity.ok(realTimeStockDTOList);
    }


}
