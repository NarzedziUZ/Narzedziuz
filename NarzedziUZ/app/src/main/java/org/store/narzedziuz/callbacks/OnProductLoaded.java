package org.store.narzedziuz.callbacks;
import org.store.narzedziuz.models.Product;
public interface OnProductLoaded {
    void onSuccess(Product product);
    void onFailure(Exception e);
}
