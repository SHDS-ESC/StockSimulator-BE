package team.shdsesc.stocksimul.market.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Embeddable
public class ReportId implements Serializable {

    @Column(name = "report_date")
    private LocalDateTime date;

    @Column(name = "stock_id")
    private Long stockId;

    public ReportId() {
    }

    public ReportId(LocalDateTime date, Long stockId) {
        this.date = date;
        this.stockId = stockId;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Long getStockId() {
        return stockId;
    }

    public void setStockId(Long stockId) {
        this.stockId = stockId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReportId reportId = (ReportId) o;
        return Objects.equals(date, reportId.date) && Objects.equals(stockId, reportId.stockId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, stockId);
    }
}
