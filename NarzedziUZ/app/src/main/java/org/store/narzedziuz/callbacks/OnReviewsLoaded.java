package org.store.narzedziuz.callbacks;
import org.store.narzedziuz.models.Review;
import java.util.List;
public interface OnReviewsLoaded {
    void onSuccess(List<Review> reviews);
    void onFailure(Exception e);
}
