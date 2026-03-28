package org.store.narzedziuz.callbacks;
import org.store.narzedziuz.models.Order;
public interface OnOrderLoaded {
    void onSuccess(Order order);
    void onFailure(Exception e);
}
