package team.shdsesc.stocksimul.market.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "report")
@Getter
@Setter
@NoArgsConstructor
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

    @Transient
    private Double stochastic;

    @Column(name = "obv")
    private Double obv;

    // 특별한 로직이 있는 메서드들은 롬복으로 대체할 수 없으므로 유지
    public LocalDateTime getDate() {
        return id != null ? id.getDate() : null;
    }

    public Long getStockId() {
        return id != null ? id.getStockId() : null;
    }
}
