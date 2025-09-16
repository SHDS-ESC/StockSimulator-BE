package team.shdsesc.stocksimul.market.dto;

import java.util.List;

public class TickersResponse {
    private String s;
    private List<String> tickers;

    public TickersResponse(String s, List<String> tickers) {
        this.s = s;
        this.tickers = tickers;
    }

    public String getS() { return s; }
    public void setS(String s) { this.s = s; }
    public List<String> getTickers() { return tickers; }
    public void setTickers(List<String> tickers) { this.tickers = tickers; }
}



