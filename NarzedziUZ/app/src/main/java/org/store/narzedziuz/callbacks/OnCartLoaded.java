package org.store.narzedziuz.callbacks;
import org.store.narzedziuz.models.CartItem;
import java.util.List;
public interface OnCartLoaded {
    void onSuccess(List<CartItem> items);
    void onFailure(Exception e);
}
