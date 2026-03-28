package org.store.narzedziuz.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;

public class Product {
    @DocumentId
    private String id;
    private String name;
    private String description;
    private double price;
    private int quantity;
    private String categoryId;
    private String manufacturer;
    private String photoUrl;

    public Product() {}

    public Product(String name, String description, double price, int quantity,
                   String categoryId, String manufacturer, String photoUrl) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.categoryId = categoryId;
        this.manufacturer = manufacturer;
        this.photoUrl = photoUrl;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    @Exclude
    public String getFormattedPrice() {
        return String.format("%.2f PLN", price);
    }

    @Exclude
    public boolean isInStock() {
        return quantity > 0;
    }
}
