package team.shdsesc.stocksimul.stock;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import java.io.IOException;

@Log4j2
@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;

//    private final OkHttpClient okHttpClient;

    public StockEntity findById(String id) {
        return null;
    }

    @Override
    public List<StockEntity> findAll() { return stockRepository.findAll(); }

    @Override
    public StockEntity findByTicker(String ticker) {
        return stockRepository.findByTicker(ticker);
    }

    @Override
    public List<RealTimeStockDTO> getRealTime(String ticker) {

        List<RealTimeStockDTO> realTimeStockDTOList = new ArrayList<>();

        String INTERVAL = "15min";

        OkHttpClient client = new OkHttpClient();
        Gson gson = new Gson();

        String url = String.format(
                "https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=%s&interval=%s&apikey=%s",
                ticker, INTERVAL, "undefined");

        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            String jsonData = response.body().string();
            JsonObject jsonObject = gson.fromJson(jsonData, JsonObject.class);

            // Time Series 데이터 추출
            JsonObject timeSeries = jsonObject.getAsJsonObject(String.format("Time Series (%s)", INTERVAL));

            // 데이터 출력 예시
            for (String timestamp : timeSeries.keySet()) {
                JsonElement data = timeSeries.get(timestamp);
                Double close = data.getAsJsonObject().get("4. close").getAsDouble();
                System.out.println("Time: " + timestamp);
                System.out.println("Close Price: " + close);

                RealTimeStockDTO realTimeStockDTO = new RealTimeStockDTO();
                realTimeStockDTO.setTicker(ticker);
                realTimeStockDTO.setClose(close);
                realTimeStockDTO.setTime(timestamp);

                realTimeStockDTOList.add(realTimeStockDTO);

            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return realTimeStockDTOList;
    }
}
