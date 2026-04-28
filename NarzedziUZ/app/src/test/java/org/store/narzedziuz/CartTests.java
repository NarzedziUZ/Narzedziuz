package org.store.narzedziuz;



import org.testng.annotations.Test;
import org.mockito.Mockito;
import org.store.narzedziuz.models.CartItem;
import org.store.narzedziuz.models.DiscountCode;
import org.store.narzedziuz.utils.CartCalculator;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class CartTests {

    @Test
    public void shouldSomething() { }

    @Test
    public void shouldCalculateSubtotal() {
        CartItem item1 = Mockito.mock(CartItem.class);
        CartItem item2 = Mockito.mock(CartItem.class);

        Mockito.when(item1.getTotalPrice()).thenReturn(100.0);
        Mockito.when(item2.getTotalPrice()).thenReturn(50.0);

        List<CartItem> items = Arrays.asList(item1, item2);

        double result = CartCalculator.calculateSubtotal(items);

        assertEquals(150.0, result, 0.01);
    }

    @Test
    public void shouldReturnSameTotalWithoutDiscount() {
        double total = CartCalculator.calculateTotal(200, null);
        assertEquals(200, total, 0.01);
    }

    @Test
    public void shouldApplyDiscountCorrectly() {
        DiscountCode dc = new DiscountCode();
        dc.setPercent(20);

        double total = CartCalculator.calculateTotal(200, dc);

        assertEquals(160, total, 0.01);
    }

    @Test
    public void shouldCalculateDiscountAmount() {
        DiscountCode dc = new DiscountCode();
        dc.setPercent(10);

        double discount = CartCalculator.calculateDiscountAmount(300, dc);

        assertEquals(30, discount, 0.01);
    }

    @Test
    public void shouldDetectMissingRequiredProduct() {
        CartItem item = Mockito.mock(CartItem.class);
        Mockito.when(item.getProductId()).thenReturn("A");

        DiscountCode dc = new DiscountCode();
        dc.setProductId("B");

        boolean hasProduct = false;

        for (CartItem i : Arrays.asList(item)) {
            if (dc.getProductId().equals(i.getProductId())) {
                hasProduct = true;
            }
        }

        assertFalse(hasProduct);
    }
}
