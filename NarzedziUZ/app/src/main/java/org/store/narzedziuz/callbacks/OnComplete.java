package org.store.narzedziuz.callbacks;
public interface OnComplete {
    void onSuccess();
    void onFailure(Exception e);
}
