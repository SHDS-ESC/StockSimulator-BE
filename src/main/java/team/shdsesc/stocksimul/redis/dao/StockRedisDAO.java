package team.shdsesc.stocksimul.redis.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import team.shdsesc.stocksimul.market.dto.RealTimeStockDTO;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class StockRedisDAO {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String STOCK_KEY_PREFIX = "stock:";
    private static final String STOCK_KEYS_SET = "stock:keys";

    // 주식 데이터 저장
    public void saveStock(RealTimeStockDTO stock) {
        String key = STOCK_KEY_PREFIX + stock.getSymbol();
        redisTemplate.opsForValue().set(key, stock);
        redisTemplate.opsForSet().add(STOCK_KEYS_SET, stock.getSymbol());
    }

    // 특정 주식 데이터 조회
    public RealTimeStockDTO getStock(String symbol) {
        String key = STOCK_KEY_PREFIX + symbol;
        Object stock = redisTemplate.opsForValue().get(key);
        if (stock instanceof RealTimeStockDTO) {
            return (RealTimeStockDTO) stock;
        }
        return null;
    }

    // 모든 주식 데이터 조회
    public List<RealTimeStockDTO> getAllStocks() {
        Set<Object> symbols = redisTemplate.opsForSet().members(STOCK_KEYS_SET);
        if (symbols == null || symbols.isEmpty()) {
            return List.of();
        }

        return symbols.stream()
                .map(symbol -> getStock(symbol.toString()))
                .filter(stock -> stock != null)
                .collect(Collectors.toList());
    }

    // 모든 주식 데이터 삭제
    public void deleteAllStocks() {
        Set<Object> symbols = redisTemplate.opsForSet().members(STOCK_KEYS_SET);
        if (symbols != null && !symbols.isEmpty()) {
            for (Object symbol : symbols) {
                String key = STOCK_KEY_PREFIX + symbol.toString();
                redisTemplate.delete(key);
            }
            redisTemplate.delete(STOCK_KEYS_SET);
        }
    }

    // 주식 개수 조회
    public long getStockCount() {
        return redisTemplate.opsForSet().size(STOCK_KEYS_SET);
    }
}
