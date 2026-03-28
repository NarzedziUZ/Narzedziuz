package org.store.narzedziuz.callbacks;
import org.store.narzedziuz.models.Order;
import java.util.List;
public interface OnOrdersLoaded {
    void onSuccess(List<Order> orders);
    void onFailure(Exception e);
}
