package org.store.narzedziuz.repositories;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.store.narzedziuz.callbacks.OnComplete;
import org.store.narzedziuz.callbacks.OnReviewsLoaded;
import org.store.narzedziuz.models.Review;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReviewRepository {

    private static final String REVIEWS = "reviews";
    private static final String ITEMS = "items";

    private final FirebaseFirestore db;
    private static ReviewRepository instance;

    private ReviewRepository() { db = FirebaseFirestore.getInstance(); }

    public static ReviewRepository getInstance() {
        if (instance == null) instance = new ReviewRepository();
        return instance;
    }

    /** Pobiera wszystkie recenzje produktu */
    public void getProductReviews(String productId, OnReviewsLoaded callback) {
        db.collection(REVIEWS).document(productId).collection(ITEMS)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Review> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Review r = doc.toObject(Review.class);
                        r.setId(doc.getId());
                        list.add(r);
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /** Pobiera recenzję danego użytkownika dla produktu (null jeśli brak) */
    public void getUserReviewForProduct(String productId, String userId, OnReviewsLoaded callback) {
        db.collection(REVIEWS).document(productId).collection(ITEMS)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Review> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Review r = doc.toObject(Review.class);
                        r.setId(doc.getId());
                        list.add(r);
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /** Dodaje nową recenzję */
    public void addReview(Review review, OnComplete callback) {
        db.collection(REVIEWS).document(review.getProductId()).collection(ITEMS)
                .add(review)
                .addOnSuccessListener(ref -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    /** Aktualizuje istniejącą recenzję */
    public void updateReview(String productId, String reviewId, int rating, String comment, OnComplete callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("rating", rating);
        updates.put("comment", comment);
        db.collection(REVIEWS).document(productId).collection(ITEMS)
                .document(reviewId)
                .update(updates)
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    /** Usuwa recenzję */
    public void deleteReview(String productId, String reviewId, OnComplete callback) {
        db.collection(REVIEWS).document(productId).collection(ITEMS)
                .document(reviewId)
                .delete()
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }
}
