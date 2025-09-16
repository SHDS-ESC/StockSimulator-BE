package team.shdsesc.stocksimul.market.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "watchlist")
public class Watchlist {

    @EmbeddedId
    private WatchlistId id;

    public WatchlistId getId() {
        return id;
    }

    public void setId(WatchlistId id) {
        this.id = id;
    }

    public String getUserId() {
        return id != null ? id.getUserId() : null;
    }

    public String getTicker() {
        return id != null ? id.getTicker() : null;
    }
}



