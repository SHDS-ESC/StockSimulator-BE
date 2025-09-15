package team.shdsesc.stocksimul.market.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "stock_info")
public class StockInfo {

    @Id
    @Column(name = "ticker")
    private String ticker;

    // 원본 컬럼명은 security(회사명). 서비스 단에서는 name으로 매핑해 사용
    @Column(name = "security")
    private String security;

    @Column(name = "sector")
    private String sector;

    @Column(name = "industry")
    private String industry;

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getSecurity() {
        return security;
    }

    public void setSecurity(String security) {
        this.security = security;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }
}



