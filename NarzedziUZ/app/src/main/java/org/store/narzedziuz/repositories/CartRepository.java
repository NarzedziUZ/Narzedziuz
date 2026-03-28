package org.store.narzedziuz.repositories;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.store.narzedziuz.callbacks.OnCartLoaded;
import org.store.narzedziuz.callbacks.OnComplete;
import org.store.narzedziuz.models.CartItem;
import org.store.narzedziuz.models.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CartRepository {

    private static final String USERS = "users";
    private static final String CART = "cart";

    private final FirebaseFirestore db;
    private static CartRepository instance;

    private CartRepository() { db = FirebaseFirestore.getInstance(); }

    public static CartRepository getInstance() {
        if (instance == null) instance = new CartRepository();
        return instance;
    }

    /** Pobiera koszyk użytkownika z uzupełnionymi danymi produktów */
    public void getCart(String userId, OnCartLoaded callback) {
        db.collection(USERS).document(userId).collection(CART)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<CartItem> items = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        CartItem item = doc.toObject(CartItem.class);
                        item.setId(doc.getId());
                        items.add(item);
                    }
                    if (items.isEmpty()) {
                        callback.onSuccess(items);
                        return;
                    }
                    // Uzupełnij dane produktów
                    AtomicInteger remaining = new AtomicInteger(items.size());
                    for (CartItem item : items) {
                        ProductRepository.getInstance().getProductById(item.getProductId(),
                                new org.store.narzedziuz.callbacks.OnProductLoaded() {
                                    @Override
                                    public void onSuccess(Product product) {
                                        item.setProduct(product);
                                        if (remaining.decrementAndGet() == 0) {
                                            callback.onSuccess(items);
                                        }
                                    }
                                    @Override
                                    public void onFailure(Exception e) {
                                        if (remaining.decrementAndGet() == 0) {
                                            callback.onSuccess(items);
                                        }
                                    }
                                });
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    /** Dodaje lub aktualizuje produkt w koszyku */
    public void addToCart(String userId, String productId, int quantity, double price, OnComplete callback) {
        db.collection(USERS).document(userId).collection(CART)
                .whereEqualTo("productId", productId)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (!snapshots.isEmpty()) {
                        // Produkt już w koszyku – zwiększ ilość
                        QueryDocumentSnapshot existing = (QueryDocumentSnapshot) snapshots.getDocuments().get(0);
                        long currentQty = existing.getLong("quantity") != null ? existing.getLong("quantity") : 0;
                        existing.getReference()
                                .update("quantity", currentQty + quantity)
                                .addOnSuccessListener(v -> callback.onSuccess())
                                .addOnFailureListener(callback::onFailure);
                    } else {
                        // Nowy wpis
                        CartItem newItem = new CartItem(productId, quantity, price);
                        db.collection(USERS).document(userId).collection(CART)
                                .add(newItem)
                                .addOnSuccessListener(v -> callback.onSuccess())
                                .addOnFailureListener(callback::onFailure);
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    /** Usuwa pozycję z koszyka */
    public void removeFromCart(String userId, String cartItemId, OnComplete callback) {
        db.collection(USERS).document(userId).collection(CART)
                .document(cartItemId)
                .delete()
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    /** Aktualizuje ilość pozycji */
    public void updateQuantity(String userId, String cartItemId, int quantity, OnComplete callback) {
        db.collection(USERS).document(userId).collection(CART)
                .document(cartItemId)
                .update("quantity", quantity)
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    /** Czyści cały koszyk po złożeniu zamówienia */
    public void clearCart(String userId, OnComplete callback) {
        db.collection(USERS).document(userId).collection(CART)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (snapshots.isEmpty()) {
                        callback.onSuccess();
                        return;
                    }
                    AtomicInteger remaining = new AtomicInteger(snapshots.size());
                    for (QueryDocumentSnapshot doc : snapshots) {
                        doc.getReference().delete()
                                .addOnSuccessListener(v -> {
                                    if (remaining.decrementAndGet() == 0) callback.onSuccess();
                                })
                                .addOnFailureListener(e -> {
                                    if (remaining.decrementAndGet() == 0) callback.onSuccess();
                                });
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }
}
