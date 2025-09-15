package team.shdsesc.stocksimul.market.dto;

import java.util.ArrayList;
import java.util.List;

public class CandleResponse {

    private String s;
    private List<Long> t;
    private List<String> d;
    private List<Double> o;
    private List<Double> h;
    private List<Double> l;
    private List<Double> c;
    private List<Double> v;

    public static CandleResponse noData() {
        CandleResponse r = new CandleResponse();
        r.s = "no_data";
        r.t = new ArrayList<>();
        r.d = new ArrayList<>();
        r.o = new ArrayList<>();
        r.h = new ArrayList<>();
        r.l = new ArrayList<>();
        r.c = new ArrayList<>();
        r.v = new ArrayList<>();
        return r;
    }

    public CandleResponse() {}

    public CandleResponse(String s, List<Long> t, List<String> d, List<Double> o, List<Double> h, List<Double> l, List<Double> c, List<Double> v) {
        this.s = s;
        this.t = t;
        this.d = d;
        this.o = o;
        this.h = h;
        this.l = l;
        this.c = c;
        this.v = v;
    }

    public String getS() { return s; }
    public void setS(String s) { this.s = s; }
    public List<Long> getT() { return t; }
    public void setT(List<Long> t) { this.t = t; }
    public List<String> getD() { return d; }
    public void setD(List<String> d) { this.d = d; }
    public List<Double> getO() { return o; }
    public void setO(List<Double> o) { this.o = o; }
    public List<Double> getH() { return h; }
    public void setH(List<Double> h) { this.h = h; }
    public List<Double> getL() { return l; }
    public void setL(List<Double> l) { this.l = l; }
    public List<Double> getC() { return c; }
    public void setC(List<Double> c) { this.c = c; }
    public List<Double> getV() { return v; }
    public void setV(List<Double> v) { this.v = v; }
}



