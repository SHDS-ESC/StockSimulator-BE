package team.shdsesc.stocksimul.stock;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.sql.Time;

@Getter
@Setter
public class RealTimeStockDTO {


    private String ticker;
    private Double close;
    private String time;

}
