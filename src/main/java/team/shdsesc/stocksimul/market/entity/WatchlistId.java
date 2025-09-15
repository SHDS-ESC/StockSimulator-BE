package team.shdsesc.stocksimul.market.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class WatchlistId implements Serializable {

    @Column(name = "user_id", length = 100, nullable = false)
    private String userId;

    @Column(name = "ticker", length = 32, nullable = false)
    private String ticker;

    public WatchlistId() {
    }

    public WatchlistId(String userId, String ticker) {
        this.userId = userId;
        this.ticker = ticker;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WatchlistId that = (WatchlistId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(ticker, that.ticker);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, ticker);
    }
}



