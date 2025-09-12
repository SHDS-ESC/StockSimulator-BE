package team.shdsesc.stocksimul.stock.report;

import jakarta.persistence.*;
import lombok.*;
import team.shdsesc.stocksimul.auth.util.BaseEntity;
import team.shdsesc.stocksimul.stock.StockEntity;

import java.util.Date;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "report")
public class ReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @Column(nullable = false)
    private Date reportDate;

    @ManyToOne
    @JoinColumn(name = "stockId", nullable = false)
    private StockEntity stock;

    @Column(nullable = false)
    private Double open;
    @Column(nullable = false)
    private Double close;
    @Column(nullable = false)
    private Double high;
    @Column(nullable = false)
    private Double low;
    @Column(nullable = false)
    private Double volume;


}
