package org.store.narzedziuz.models;

public class DiscountCode {
    private String code;
    private int percent;

    public DiscountCode(String code, int percent) {
        this.code = code;
        this.percent = percent;
    }

    public String getCode() { return code; }
    public int getPercent() { return percent; }
}
