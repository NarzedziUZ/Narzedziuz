package org.store.narzedziuz.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import org.store.narzedziuz.R;
import org.store.narzedziuz.adapters.WishlistAdapter;
import org.store.narzedziuz.callbacks.OnComplete;
import org.store.narzedziuz.callbacks.OnWishlistLoaded;
import org.store.narzedziuz.models.Product;
import org.store.narzedziuz.repositories.UserRepository;

import java.util.List;

public class WishlistActivity extends AppCompatActivity {

    private RecyclerView recyclerWishlist;
    private WishlistAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.wishlist_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        recyclerWishlist = findViewById(R.id.recycler_wishlist);
        progressBar      = findViewById(R.id.progress_bar);
        tvEmpty          = findViewById(R.id.tv_empty);

        adapter = new WishlistAdapter(this, new WishlistAdapter.OnWishlistListener() {
            @Override
            public void onProductClick(Product product) {
                Intent i = new Intent(WishlistActivity.this, ProductDetailActivity.class);
                i.putExtra("productId", product.getId());
                startActivity(i);
            }
            @Override
            public void onRemove(Product product, int position) {
                UserRepository.getInstance().removeFromWishlist(userId, product.getId(), new OnComplete() {
                    @Override public void onSuccess() { loadWishlist(); }
                    @Override public void onFailure(Exception e) {
                        Toast.makeText(WishlistActivity.this, "Błąd usuwania", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        recyclerWishlist.setLayoutManager(new LinearLayoutManager(this));
        recyclerWishlist.setAdapter(adapter);

        loadWishlist();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWishlist();
    }

    private void loadWishlist() {
        progressBar.setVisibility(View.VISIBLE);
        UserRepository.getInstance().getWishlist(userId, new OnWishlistLoaded() {
            @Override
            public void onSuccess(List<Product> products) {
                progressBar.setVisibility(View.GONE);
                adapter.setProducts(products);
                tvEmpty.setVisibility(products.isEmpty() ? View.VISIBLE : View.GONE);
            }
            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(WishlistActivity.this, "Błąd ładowania listy życzeń", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
