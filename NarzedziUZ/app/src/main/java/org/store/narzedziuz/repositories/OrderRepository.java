package org.store.narzedziuz.repositories;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.store.narzedziuz.callbacks.OnComplete;
import org.store.narzedziuz.callbacks.OnOrderLoaded;
import org.store.narzedziuz.callbacks.OnOrdersLoaded;
import org.store.narzedziuz.models.Order;

import java.util.ArrayList;
import java.util.List;

public class OrderRepository {

    private static final String USERS = "users";
    private static final String ORDERS = "orders";

    private final FirebaseFirestore db;
    private static OrderRepository instance;

    private OrderRepository() { db = FirebaseFirestore.getInstance(); }

    public static OrderRepository getInstance() {
        if (instance == null) instance = new OrderRepository();
        return instance;
    }

    /** Tworzy nowe zamówienie i zwraca jego ID */
    public void createOrder(Order order, OnOrderLoaded callback) {
        db.collection(USERS).document(order.getUserId()).collection(ORDERS)
                .add(order)
                .addOnSuccessListener(ref -> {
                    order.setId(ref.getId());
                    callback.onSuccess(order);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /** Pobiera wszystkie zamówienia użytkownika, posortowane od najnowszego */
    public void getUserOrders(String userId, OnOrdersLoaded callback) {
        db.collection(USERS).document(userId).collection(ORDERS)
                .orderBy("orderDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Order> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Order o = doc.toObject(Order.class);
                        o.setId(doc.getId());
                        list.add(o);
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /** Pobiera szczegóły zamówienia */
    public void getOrderById(String userId, String orderId, OnOrderLoaded callback) {
        db.collection(USERS).document(userId).collection(ORDERS)
                .document(orderId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Order o = doc.toObject(Order.class);
                        if (o != null) o.setId(doc.getId());
                        callback.onSuccess(o);
                    } else {
                        callback.onFailure(new Exception("Zamówienie nie istnieje"));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }
}
