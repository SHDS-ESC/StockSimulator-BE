package team.shdsesc.stocksimul.market.dto;

public class RangeResponse {
    private String s;
    private long last;
    private long from;
    private long to;

    public static RangeResponse noData() {
        RangeResponse r = new RangeResponse();
        r.s = "no_data";
        return r;
    }

    public RangeResponse() {}

    public RangeResponse(String s, long last, long from, long to) {
        this.s = s;
        this.last = last;
        this.from = from;
        this.to = to;
    }

    public String getS() { return s; }
    public void setS(String s) { this.s = s; }
    public long getLast() { return last; }
    public void setLast(long last) { this.last = last; }
    public long getFrom() { return from; }
    public void setFrom(long from) { this.from = from; }
    public long getTo() { return to; }
    public void setTo(long to) { this.to = to; }
}



