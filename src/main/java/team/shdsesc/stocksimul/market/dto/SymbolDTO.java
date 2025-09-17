package team.shdsesc.stocksimul.market.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SymbolDTO {
    private String ticker;
    private String name;
    private String sector;
    private String industry;


}



