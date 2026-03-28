package org.store.narzedziuz.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.store.narzedziuz.R;
import org.store.narzedziuz.adapters.ReviewAdapter;
import org.store.narzedziuz.callbacks.OnComplete;
import org.store.narzedziuz.callbacks.OnProductLoaded;
import org.store.narzedziuz.callbacks.OnReviewsLoaded;
import org.store.narzedziuz.models.Product;
import org.store.narzedziuz.models.Review;
import org.store.narzedziuz.repositories.CartRepository;
import org.store.narzedziuz.repositories.ProductRepository;
import org.store.narzedziuz.repositories.ReviewRepository;
import org.store.narzedziuz.repositories.UserRepository;

import java.util.List;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView imgProduct;
    private TextView tvName, tvManufacturer, tvPrice, tvStock, tvDescription, tvAvgRating, tvReviewCount;
    private Button btnAddToCart, btnWishlist, btnAddReview;
    private RatingBar ratingBarAvg;
    private RecyclerView recyclerReviews;
    private ProgressBar progressBar;

    private ReviewAdapter reviewAdapter;
    private Product product;
    private String productId;
    private boolean isInWishlist = false;
    private Review userReview = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        imgProduct      = findViewById(R.id.img_product);
        tvName          = findViewById(R.id.tv_name);
        tvManufacturer  = findViewById(R.id.tv_manufacturer);
        tvPrice         = findViewById(R.id.tv_price);
        tvStock         = findViewById(R.id.tv_stock);
        tvDescription   = findViewById(R.id.tv_description);
        tvAvgRating     = findViewById(R.id.tv_avg_rating);
        tvReviewCount   = findViewById(R.id.tv_review_count);
        ratingBarAvg    = findViewById(R.id.rating_bar_avg);
        btnAddToCart    = findViewById(R.id.btn_add_to_cart);
        btnWishlist     = findViewById(R.id.btn_wishlist);
        btnAddReview    = findViewById(R.id.btn_add_review);
        recyclerReviews = findViewById(R.id.recycler_reviews);
        progressBar     = findViewById(R.id.progress_bar);

        reviewAdapter = new ReviewAdapter(this);
        recyclerReviews.setLayoutManager(new LinearLayoutManager(this));
        recyclerReviews.setAdapter(reviewAdapter);
        recyclerReviews.setNestedScrollingEnabled(false);

        productId = getIntent().getStringExtra("productId");
        if (productId == null) { finish(); return; }

        loadProduct();
    }

    private void loadProduct() {
        progressBar.setVisibility(View.VISIBLE);
        ProductRepository.getInstance().getProductById(productId, new OnProductLoaded() {
            @Override
            public void onSuccess(Product p) {
                product = p;
                progressBar.setVisibility(View.GONE);
                displayProduct();
                loadReviews();
                checkWishlist();
            }
            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ProductDetailActivity.this, "Błąd ładowania produktu", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void displayProduct() {
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(product.getName());
        tvName.setText(product.getName());
        tvManufacturer.setText(getString(R.string.manufacturer_label, product.getManufacturer()));
        tvPrice.setText(String.format(Locale.getDefault(), "%.2f PLN", product.getPrice()));
        tvDescription.setText(product.getDescription());

        if (product.isInStock()) {
            tvStock.setText(getString(R.string.in_stock, product.getQuantity()));
            tvStock.setTextColor(getColor(R.color.green));
            btnAddToCart.setEnabled(true);
        } else {
            tvStock.setText(R.string.out_of_stock);
            tvStock.setTextColor(getColor(R.color.red));
            btnAddToCart.setEnabled(false);
        }

        if (product.getPhotoUrl() != null && !product.getPhotoUrl().isEmpty()) {
            Glide.with(this).load(product.getPhotoUrl())
                    .placeholder(R.drawable.ic_product_placeholder).into(imgProduct);
        } else {
            imgProduct.setImageResource(R.drawable.ic_product_placeholder);
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            btnAddToCart.setEnabled(false);
            btnWishlist.setEnabled(false);
            btnAddReview.setEnabled(false);
            btnAddToCart.setText(R.string.login_required);
        }

        btnAddToCart.setOnClickListener(v -> addToCart());
        btnWishlist.setOnClickListener(v -> toggleWishlist());
        btnAddReview.setOnClickListener(v -> showReviewDialog(null));
    }

    private void addToCart() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }
        CartRepository.getInstance().addToCart(user.getUid(), product.getId(), 1, product.getPrice(),
                new OnComplete() {
                    @Override public void onSuccess() {
                        Toast.makeText(ProductDetailActivity.this, getString(R.string.added_to_cart), Toast.LENGTH_SHORT).show();
                    }
                    @Override public void onFailure(Exception e) {
                        Toast.makeText(ProductDetailActivity.this, "Błąd dodawania do koszyka", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkWishlist() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        UserRepository.getInstance().isInWishlist(user.getUid(), productId, inWishlist -> {
            isInWishlist = inWishlist;
            btnWishlist.setText(isInWishlist ? R.string.in_wishlist : R.string.add_to_wishlist);
        });
    }

    private void toggleWishlist() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) { startActivity(new Intent(this, LoginActivity.class)); return; }
        OnComplete cb = new OnComplete() {
            @Override public void onSuccess() {
                isInWishlist = !isInWishlist;
                btnWishlist.setText(isInWishlist ? R.string.in_wishlist : R.string.add_to_wishlist);
            }
            @Override public void onFailure(Exception e) {
                Toast.makeText(ProductDetailActivity.this, "Błąd aktualizacji listy życzeń", Toast.LENGTH_SHORT).show();
            }
        };
        if (isInWishlist) {
            UserRepository.getInstance().removeFromWishlist(user.getUid(), productId, cb);
        } else {
            UserRepository.getInstance().addToWishlist(user.getUid(), productId, cb);
        }
    }

    private void loadReviews() {
        ReviewRepository.getInstance().getProductReviews(productId, new OnReviewsLoaded() {
            @Override
            public void onSuccess(List<Review> reviews) {
                reviewAdapter.setReviews(reviews);
                if (!reviews.isEmpty()) {
                    double avg = reviews.stream().mapToInt(Review::getRating).average().orElse(0);
                    tvAvgRating.setText(String.format(Locale.getDefault(), "%.1f", avg));
                    tvReviewCount.setText(getString(R.string.review_count, reviews.size()));
                    ratingBarAvg.setRating((float) avg);
                } else {
                    tvAvgRating.setText("0.0");
                    tvReviewCount.setText(R.string.no_reviews_yet);
                    ratingBarAvg.setRating(0);
                }
                // Sprawdź czy bieżący user już ocenił
                FirebaseUser fu = FirebaseAuth.getInstance().getCurrentUser();
                if (fu != null) {
                    for (Review r : reviews) {
                        if (fu.getUid().equals(r.getUserId())) {
                            userReview = r;
                            btnAddReview.setText(R.string.edit_review);
                            break;
                        }
                    }
                }
            }
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(ProductDetailActivity.this, "Błąd ładowania recenzji", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showReviewDialog(Review existingReview) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) { startActivity(new Intent(this, LoginActivity.class)); return; }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_review, null);
        RatingBar rb    = dialogView.findViewById(R.id.rating_bar_input);
        EditText etCom  = dialogView.findViewById(R.id.et_comment);

        if (existingReview != null) {
            rb.setRating(existingReview.getRating());
            etCom.setText(existingReview.getComment());
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(existingReview == null ? getString(R.string.add_review) : getString(R.string.edit_review))
                .setView(dialogView)
                .setPositiveButton(existingReview == null ? getString(R.string.submit) : getString(R.string.update), null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            int rating    = (int) rb.getRating();
            String comment = etCom.getText().toString().trim();
            if (rating == 0) { Toast.makeText(this, "Wybierz ocenę", Toast.LENGTH_SHORT).show(); return; }

            if (existingReview == null) {
                UserRepository.getInstance().getUser(user.getUid(), new UserRepository.UserCallback() {
                    @Override public void onSuccess(org.store.narzedziuz.models.AppUser appUser) {
                        Review r = new Review(user.getUid(), appUser.getFullName(), productId, rating, comment);
                        ReviewRepository.getInstance().addReview(r, new OnComplete() {
                            @Override public void onSuccess() { dialog.dismiss(); loadReviews(); }
                            @Override public void onFailure(Exception e) {
                                Toast.makeText(ProductDetailActivity.this, "Błąd zapisu recenzji", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    @Override public void onFailure(Exception e) {}
                });
            } else {
                ReviewRepository.getInstance().updateReview(productId, existingReview.getId(), rating, comment, new OnComplete() {
                    @Override public void onSuccess() { dialog.dismiss(); loadReviews(); }
                    @Override public void onFailure(Exception e) {
                        Toast.makeText(ProductDetailActivity.this, "Błąd aktualizacji recenzji", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }));

        // Przycisk usuń (tylko gdy edytujemy)
        if (existingReview != null) {
            dialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.delete_review), (d, which) -> {
                ReviewRepository.getInstance().deleteReview(productId, existingReview.getId(), new OnComplete() {
                    @Override public void onSuccess() { userReview = null; btnAddReview.setText(R.string.add_review_btn); loadReviews(); }
                    @Override public void onFailure(Exception e) {}
                });
            });
        }
        dialog.show();
    }
}
