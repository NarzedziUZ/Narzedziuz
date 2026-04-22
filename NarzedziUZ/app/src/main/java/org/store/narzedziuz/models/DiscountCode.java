package org.store.narzedziuz.models;

public class DiscountCode {
    private String code;
    private int percent;
    private String productId;
    private String userId; // Nowe pole na ID użytkownika
    private String generatedDate;

    public DiscountCode() {
        // Wymagane przez Firebase
    }
    public DiscountCode(String code, int percent) {
        this.code = code;
        this.percent = percent;
    }

    public DiscountCode(String code, int percent, String productId, String userId, String generatedDate) {
        this.code = code;
        this.percent = percent;
        this.productId = productId;
        this.userId = userId;
        this.generatedDate = generatedDate;
    }

    // Gettery i Settery
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public int getPercent() { return percent; }
    public void setPercent(int percent) { this.percent = percent; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getGeneratedDate() { return generatedDate; }
    public void setGeneratedDate(String generatedDate) { this.generatedDate = generatedDate; }
}