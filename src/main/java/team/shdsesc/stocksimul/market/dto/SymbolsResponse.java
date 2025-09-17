package team.shdsesc.stocksimul.market.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SymbolsResponse {
    private String status;
    private List<SymbolDTO> symbols;

}



