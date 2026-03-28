package org.store.narzedziuz.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Order {
    @DocumentId
    private String id;
    private String userId;
    @ServerTimestamp
    private Date orderDate;
    private String status;
    private double totalPrice;
    private String deliveryAddress;
    private String paymentMethod;
    private List<OrderItem> items;

    public Order() {
        this.items = new ArrayList<>();
        this.status = "NOWE";
    }

    public Order(String userId, String deliveryAddress, String paymentMethod, double totalPrice) {
        this.userId = userId;
        this.deliveryAddress = deliveryAddress;
        this.paymentMethod = paymentMethod;
        this.totalPrice = totalPrice;
        this.status = "NOWE";
        this.items = new ArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public Date getOrderDate() { return orderDate; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public String getFormattedDate() {
        if (orderDate == null) return "";
        return new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(orderDate);
    }

    public String getFormattedTotal() {
        return String.format(Locale.getDefault(), "%.2f PLN", totalPrice);
    }
}
