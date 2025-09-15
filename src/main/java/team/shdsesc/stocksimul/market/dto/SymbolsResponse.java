package team.shdsesc.stocksimul.market.dto;

import java.util.List;

public class SymbolsResponse {
    private String s;
    private List<SymbolDTO> symbols;

    public SymbolsResponse() {}

    public SymbolsResponse(String s, List<SymbolDTO> symbols) {
        this.s = s;
        this.symbols = symbols;
    }

    public String getS() { return s; }
    public void setS(String s) { this.s = s; }
    public List<SymbolDTO> getSymbols() { return symbols; }
    public void setSymbols(List<SymbolDTO> symbols) { this.symbols = symbols; }
}



