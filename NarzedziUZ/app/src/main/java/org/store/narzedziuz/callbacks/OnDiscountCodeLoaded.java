package org.store.narzedziuz.callbacks;

import org.store.narzedziuz.models.DiscountCode;

public interface OnDiscountCodeLoaded {
    void onSuccess(DiscountCode discountCode);
    void onFailure(String errorMessage);
}