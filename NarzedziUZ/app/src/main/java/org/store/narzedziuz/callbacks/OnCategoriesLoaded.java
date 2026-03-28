package org.store.narzedziuz.callbacks;
import org.store.narzedziuz.models.Category;
import java.util.List;
public interface OnCategoriesLoaded {
    void onSuccess(List<Category> categories);
    void onFailure(Exception e);
}
