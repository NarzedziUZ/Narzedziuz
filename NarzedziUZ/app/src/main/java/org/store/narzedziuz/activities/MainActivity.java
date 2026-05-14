package org.store.narzedziuz.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner;
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;
import com.google.android.material.textfield.TextInputLayout;

import org.store.narzedziuz.R;
import org.store.narzedziuz.adapters.ProductAdapter;
import org.store.narzedziuz.callbacks.OnCategoriesLoaded;
import org.store.narzedziuz.callbacks.OnProductsLoaded;
import org.store.narzedziuz.models.Category;
import org.store.narzedziuz.models.Product;
import org.store.narzedziuz.repositories.ProductRepository;
import org.store.narzedziuz.utils.NotificationHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import androidx.appcompat.app.AppCompatDelegate;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private EditText etSearch;
    private Spinner spinnerCategory, spinnerSort;
    private SwipeRefreshLayout swipeRefresh;

    private List<Product> allProducts = new ArrayList<>();
    private List<Category> categories = new ArrayList<>();
    private String selectedCategoryId = null;

    private static final int PERMISSION_REQUEST_CODE = 112;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestNotificationPermission();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView    = findViewById(R.id.recycler_products);
        progressBar     = findViewById(R.id.progress_bar);
        tvEmpty         = findViewById(R.id.tv_empty);
        etSearch        = findViewById(R.id.et_search);
        spinnerCategory = findViewById(R.id.spinner_category);
        spinnerSort     = findViewById(R.id.spinner_sort);
        swipeRefresh    = findViewById(R.id.swipe_refresh);

        adapter = new ProductAdapter(this, product -> {
            Intent intent = new Intent(this, ProductDetailActivity.class);
            intent.putExtra("productId", product.getId());
            startActivity(intent);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(() -> {
            loadProducts();
            swipeRefresh.setRefreshing(false);
        });

        setupSearch();
        setupSortSpinner();
        loadCategories();
        loadProducts();

        View fabScanner = findViewById(R.id.search_layout);
        if (fabScanner != null) {
            fabScanner.setOnClickListener(v -> {
                startBarcodeScanner();
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_cart) {
            startActivity(new Intent(this, CartActivity.class));
            return true;
        } else if (id == R.id.action_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        } else if (id == R.id.action_wishlist) {
            startActivity(new Intent(this, WishlistActivity.class));
            return true;
        } else if (id == R.id.action_discount_draw) {
            startActivity(new Intent(this, AccelerometerActivity.class));
            return true;
        }else if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        else if (id == R.id.action_dark_mode) {

            int mode = AppCompatDelegate.getDefaultNightMode();

            if (mode == AppCompatDelegate.MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Powiadomienia włączone", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupSortSpinner() {
        String[] sortOptions = {"Domyślne", "Cena rosnąco", "Cena malejąco", "Nazwa A-Z"};
        ArrayAdapter<String> sa = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sortOptions);
        sa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(sa);
        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { applyFilters(); }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });
    }

    private void loadCategories() {
        ProductRepository.getInstance().getCategories(new OnCategoriesLoaded() {
            @Override
            public void onSuccess(List<Category> loaded) {
                categories = loaded;
                List<String> names = new ArrayList<>();
                names.add("Wszystkie kategorie");
                for (Category c : loaded) names.add(c.getName());

                ArrayAdapter<String> ca = new ArrayAdapter<>(MainActivity.this,
                        android.R.layout.simple_spinner_item, names);
                ca.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategory.setAdapter(ca);
                spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                        selectedCategoryId = pos == 0 ? null : categories.get(pos - 1).getId();
                        applyFilters();
                    }
                    @Override public void onNothingSelected(AdapterView<?> p) {}
                });
            }
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(MainActivity.this, "Błąd ładowania kategorii", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProducts() {
        progressBar.setVisibility(View.VISIBLE);
        ProductRepository.getInstance().getAllProducts(new OnProductsLoaded() {
            @Override
            public void onSuccess(List<Product> products) {
                progressBar.setVisibility(View.GONE);
                allProducts = products;
                applyFilters();
            }
            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Błąd ładowania produktów", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilters() {
        String query = etSearch.getText().toString().trim().toLowerCase();
        int sortPos  = spinnerSort.getSelectedItemPosition();

        List<Product> filtered = new ArrayList<>();
        for (Product p : allProducts) {
            boolean matchCategory = selectedCategoryId == null || selectedCategoryId.equals(p.getCategoryId());
            boolean matchSearch   = query.isEmpty()
                    || p.getName().toLowerCase().contains(query)
                    || (p.getManufacturer() != null && p.getManufacturer().toLowerCase().contains(query));
            if (matchCategory && matchSearch) filtered.add(p);
        }

        switch (sortPos) {
            case 1: filtered.sort(Comparator.comparingDouble(Product::getPrice)); break;
            case 2: filtered.sort((a, b) -> Double.compare(b.getPrice(), a.getPrice())); break;
            case 3: filtered.sort(Comparator.comparing(Product::getName)); break;
        }

        adapter.setProducts(filtered);
        tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void startBarcodeScanner() {
        GmsBarcodeScannerOptions options = new GmsBarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .enableAutoZoom()
                .build();

        GmsBarcodeScanner scanner = GmsBarcodeScanning.getClient(this, options);

        scanner.startScan()
                .addOnSuccessListener(barcode -> {
                    String scannedCode = barcode.getRawValue();
                    if (scannedCode != null) {
                        etSearch.setText(scannedCode);
                        etSearch.setSelection(scannedCode.length());

                        Toast.makeText(this, "Zeskanowano: " + scannedCode, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Błąd skanowania: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
