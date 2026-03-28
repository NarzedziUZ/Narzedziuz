package org.store.narzedziuz.callbacks;
import org.store.narzedziuz.models.Product;
import java.util.List;
public interface OnWishlistLoaded {
    void onSuccess(List<Product> products);
    void onFailure(Exception e);
}
