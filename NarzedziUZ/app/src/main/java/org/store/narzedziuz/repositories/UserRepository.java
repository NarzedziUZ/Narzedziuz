package org.store.narzedziuz.repositories;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.store.narzedziuz.callbacks.OnComplete;
import org.store.narzedziuz.callbacks.OnProductsLoaded;
import org.store.narzedziuz.callbacks.OnWishlistLoaded;
import org.store.narzedziuz.models.AppUser;
import org.store.narzedziuz.models.Product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class UserRepository {

    private static final String USERS = "users";
    private static final String WISHLIST = "wishlist";

    private final FirebaseFirestore db;
    private static UserRepository instance;

    private UserRepository() { db = FirebaseFirestore.getInstance(); }

    public static UserRepository getInstance() {
        if (instance == null) instance = new UserRepository();
        return instance;
    }

    /** Zapisuje profil użytkownika po rejestracji */
    public void saveUser(AppUser user, OnComplete callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("email", user.getEmail());
        data.put("firstName", user.getFirstName());
        data.put("lastName", user.getLastName());
        db.collection(USERS).document(user.getUid())
                .set(data)
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    /** Pobiera profil użytkownika */
    public void getUser(String uid, UserCallback callback) {
        db.collection(USERS).document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        AppUser user = new AppUser(
                                uid,
                                doc.getString("email"),
                                doc.getString("firstName"),
                                doc.getString("lastName")
                        );
                        callback.onSuccess(user);
                    } else {
                        callback.onFailure(new Exception("Użytkownik nie znaleziony"));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    /** Pobiera listę życzeń z uzupełnionymi danymi produktów */
    public void getWishlist(String userId, OnWishlistLoaded callback) {
        db.collection(USERS).document(userId).collection(WISHLIST)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (snapshots.isEmpty()) {
                        callback.onSuccess(new ArrayList<>());
                        return;
                    }
                    List<String> productIds = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        productIds.add(doc.getId());
                    }
                    // Firestore whereIn obsługuje max 30 elementów
                    if (productIds.size() > 30) productIds = productIds.subList(0, 30);
                    ProductRepository.getInstance().getProductsByIds(productIds, new OnProductsLoaded() {
                        @Override
                        public void onSuccess(List<Product> products) {
                            callback.onSuccess(products);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            callback.onFailure(e);
                        }
                    });
                })
                .addOnFailureListener(callback::onFailure);
    }

    /** Sprawdza czy produkt jest na wishliście */
    public void isInWishlist(String userId, String productId, WishlistCheckCallback callback) {
        db.collection(USERS).document(userId).collection(WISHLIST)
                .document(productId)
                .get()
                .addOnSuccessListener(doc -> callback.onResult(doc.exists()))
                .addOnFailureListener(e -> callback.onResult(false));
    }

    /** Dodaje produkt do wishlisty */
    public void addToWishlist(String userId, String productId, OnComplete callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("addedAt", FieldValue.serverTimestamp());
        db.collection(USERS).document(userId).collection(WISHLIST)
                .document(productId)
                .set(data)
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    /** Usuwa produkt z wishlisty */
    public void removeFromWishlist(String userId, String productId, OnComplete callback) {
        db.collection(USERS).document(userId).collection(WISHLIST)
                .document(productId)
                .delete()
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    public interface UserCallback {
        void onSuccess(AppUser user);
        void onFailure(Exception e);
    }

    public interface WishlistCheckCallback {
        void onResult(boolean isInWishlist);
    }
}
