package team.shdsesc.stocksimul.market.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "report")
public class Report {

    @EmbeddedId
    private ReportId id;

    @Column(name = "open")
    private Double open;

    @Column(name = "close")
    private Double close;

    @Column(name = "high")
    private Double high;

    @Column(name = "low")
    private Double low;

    @Column(name = "volume")
    private Long volume;

    @Column(name = "rsi")
    private Double rsi;

    @Column(name = "macd")
    private Double macd;

    @Column(name = "atr")
    private Double atr;

    @Column(name = "stochastic")
    private Double stochastic;

    @Column(name = "obv")
    private Double obv;

    public ReportId getId() {
        return id;
    }

    public void setId(ReportId id) {
        this.id = id;
    }

    public LocalDateTime getDate() {
        return id != null ? id.getDate() : null;
    }

    public Long getStockId() {
        return id != null ? id.getStockId() : null;
    }

    public Double getOpen() {
        return open;
    }

    public void setOpen(Double open) {
        this.open = open;
    }

    public Double getClose() {
        return close;
    }

    public void setClose(Double close) {
        this.close = close;
    }

    public Double getHigh() {
        return high;
    }

    public void setHigh(Double high) {
        this.high = high;
    }

    public Double getLow() {
        return low;
    }

    public void setLow(Double low) {
        this.low = low;
    }

    public Long getVolume() {
        return volume;
    }

    public void setVolume(Long volume) {
        this.volume = volume;
    }

    public Double getRsi() {
        return rsi;
    }

    public void setRsi(Double rsi) {
        this.rsi = rsi;
    }

    public Double getMacd() {
        return macd;
    }

    public void setMacd(Double macd) {
        this.macd = macd;
    }

    public Double getAtr() {
        return atr;
    }

    public void setAtr(Double atr) {
        this.atr = atr;
    }

    public Double getStochastic() {
        return stochastic;
    }

    public void setStochastic(Double stochastic) {
        this.stochastic = stochastic;
    }

    public Double getObv() {
        return obv;
    }

    public void setObv(Double obv) {
        this.obv = obv;
    }
}
