package team.shdsesc.stocksimul.market.dto;

public class SymbolDTO {
    private String ticker;
    private String name;
    private String sector;
    private String industry;
    private String city;
    private String ipo;
    private String ipoDate; // ISO 문자열로 직렬화

    public SymbolDTO() {}

    public SymbolDTO(String ticker, String name, String sector, String industry, String city, String ipo, String ipoDate) {
        this.ticker = ticker;
        this.name = name;
        this.sector = sector;
        this.industry = industry;
        this.city = city;
        this.ipo = ipo;
        this.ipoDate = ipoDate;
    }

    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }
    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getIpo() { return ipo; }
    public void setIpo(String ipo) { this.ipo = ipo; }

    public String getIpoDate() { return ipoDate; }
    public void setIpoDate(String ipoDate) { this.ipoDate = ipoDate; }
}



