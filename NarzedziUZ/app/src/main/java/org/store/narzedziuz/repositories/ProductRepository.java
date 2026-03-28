package org.store.narzedziuz.repositories;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.store.narzedziuz.callbacks.OnCategoriesLoaded;
import org.store.narzedziuz.callbacks.OnProductLoaded;
import org.store.narzedziuz.callbacks.OnProductsLoaded;
import org.store.narzedziuz.models.Category;
import org.store.narzedziuz.models.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductRepository {

    private static final String PRODUCTS = "products";
    private static final String CATEGORIES = "categories";

    private final FirebaseFirestore db;
    private static ProductRepository instance;

    private ProductRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public static ProductRepository getInstance() {
        if (instance == null) instance = new ProductRepository();
        return instance;
    }

    public void getAllProducts(OnProductsLoaded callback) {
        db.collection(PRODUCTS)
                .orderBy("name")
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Product> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Product p = doc.toObject(Product.class);
                        p.setId(doc.getId());
                        list.add(p);
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void getProductById(String productId, OnProductLoaded callback) {
        db.collection(PRODUCTS).document(productId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Product p = doc.toObject(Product.class);
                        if (p != null) p.setId(doc.getId());
                        callback.onSuccess(p);
                    } else {
                        callback.onFailure(new Exception("Produkt nie istnieje"));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void getProductsByCategory(String categoryId, OnProductsLoaded callback) {
        db.collection(PRODUCTS)
                .whereEqualTo("categoryId", categoryId)
                .orderBy("name")
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Product> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Product p = doc.toObject(Product.class);
                        p.setId(doc.getId());
                        list.add(p);
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void searchProducts(String query, OnProductsLoaded callback) {
        // Firestore nie obsługuje full-text search – filtrujemy po pobraniu wszystkich
        getAllProducts(new OnProductsLoaded() {
            @Override
            public void onSuccess(List<Product> products) {
                String lowerQuery = query.toLowerCase();
                List<Product> filtered = new ArrayList<>();
                for (Product p : products) {
                    if (p.getName().toLowerCase().contains(lowerQuery)
                            || (p.getManufacturer() != null && p.getManufacturer().toLowerCase().contains(lowerQuery))) {
                        filtered.add(p);
                    }
                }
                callback.onSuccess(filtered);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    public void getCategories(OnCategoriesLoaded callback) {
        db.collection(CATEGORIES)
                .orderBy("name")
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Category> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Category c = doc.toObject(Category.class);
                        c.setId(doc.getId());
                        list.add(c);
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /** Pobiera wiele produktów po ich ID (używane w wishliście) */
    public void getProductsByIds(List<String> ids, OnProductsLoaded callback) {
        if (ids == null || ids.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }
        db.collection(PRODUCTS)
                .whereIn("__name__", ids)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Product> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Product p = doc.toObject(Product.class);
                        p.setId(doc.getId());
                        list.add(p);
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(callback::onFailure);
    }
}
