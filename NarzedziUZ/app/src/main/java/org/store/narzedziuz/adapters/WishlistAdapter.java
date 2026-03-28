package org.store.narzedziuz.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.store.narzedziuz.R;
import org.store.narzedziuz.models.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder> {

    public interface OnWishlistListener {
        void onProductClick(Product product);
        void onRemove(Product product, int position);
    }

    private final Context context;
    private List<Product> products;
    private final OnWishlistListener listener;

    public WishlistAdapter(Context context, OnWishlistListener listener) {
        this.context = context;
        this.products = new ArrayList<>();
        this.listener = listener;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WishlistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_wishlist, parent, false);
        return new WishlistViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull WishlistViewHolder holder, int position) {
        holder.bind(products.get(position), position);
    }

    @Override
    public int getItemCount() { return products.size(); }

    class WishlistViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgProduct;
        private final TextView tvName, tvPrice, tvStock;
        private final ImageButton btnRemove;

        WishlistViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.img_product);
            tvName     = itemView.findViewById(R.id.tv_product_name);
            tvPrice    = itemView.findViewById(R.id.tv_price);
            tvStock    = itemView.findViewById(R.id.tv_stock);
            btnRemove  = itemView.findViewById(R.id.btn_remove);
        }

        void bind(Product product, int position) {
            tvName.setText(product.getName());
            tvPrice.setText(String.format(Locale.getDefault(), "%.2f PLN", product.getPrice()));
            if (product.isInStock()) {
                tvStock.setText(R.string.in_stock_simple);
                tvStock.setTextColor(context.getColor(R.color.green));
            } else {
                tvStock.setText(R.string.out_of_stock);
                tvStock.setTextColor(context.getColor(R.color.red));
            }
            if (product.getPhotoUrl() != null && !product.getPhotoUrl().isEmpty()) {
                Glide.with(context).load(product.getPhotoUrl())
                        .placeholder(R.drawable.ic_product_placeholder).into(imgProduct);
            } else {
                imgProduct.setImageResource(R.drawable.ic_product_placeholder);
            }
            itemView.setOnClickListener(v -> listener.onProductClick(product));
            btnRemove.setOnClickListener(v -> listener.onRemove(product, position));
        }
    }
}
