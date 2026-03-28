package org.store.narzedziuz.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;

public class CartItem {
    @DocumentId
    private String id;
    private String productId;
    private int quantity;
    private double price;

    // Transient – loaded from Products collection
    @Exclude
    private Product product;

    public CartItem() {}

    public CartItem(String productId, int quantity, double price) {
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    @Exclude
    public Product getProduct() { return product; }
    @Exclude
    public void setProduct(Product product) { this.product = product; }

    @Exclude
    public double getTotalPrice() { return price * quantity; }
}
