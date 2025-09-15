package team.shdsesc.stocksimul.market.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "watchlist")
public class Watchlist {

    @EmbeddedId
    private WatchlistId id;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}



