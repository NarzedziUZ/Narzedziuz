package org.store.narzedziuz.utils;

import org.store.narzedziuz.models.CartItem;
import org.store.narzedziuz.models.DiscountCode;

import java.util.List;

public class CartCalculator {

    public static double calculateSubtotal(List<CartItem> items) {
        double subtotal = 0;
        for (CartItem item : items) {
            subtotal += item.getTotalPrice();
        }
        return subtotal;
    }

    public static double calculateTotal(double subtotal, DiscountCode discount) {
        if (discount == null) return subtotal;
        return subtotal * (100 - discount.getPercent()) / 100.0;
    }

    public static double calculateDiscountAmount(double subtotal, DiscountCode discount) {
        if (discount == null) return 0;
        return subtotal * discount.getPercent() / 100.0;
    }
}