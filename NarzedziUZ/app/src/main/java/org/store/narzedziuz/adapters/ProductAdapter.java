package org.store.narzedziuz.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import org.store.narzedziuz.R;
import org.store.narzedziuz.models.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    private final Context context;
    private List<Product> products;
    private final OnProductClickListener listener;

    public ProductAdapter(Context context, OnProductClickListener listener) {
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
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() { return products.size(); }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgProduct;
        private final TextView tvName, tvManufacturer, tvPrice, tvStock;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.img_product);
            tvName = itemView.findViewById(R.id.tv_product_name);
            tvManufacturer = itemView.findViewById(R.id.tv_manufacturer);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvStock = itemView.findViewById(R.id.tv_stock);
        }

        void bind(Product product) {
            tvName.setText(product.getName());
            tvManufacturer.setText(product.getManufacturer());
            tvPrice.setText(String.format(Locale.getDefault(), "%.2f PLN", product.getPrice()));

            if (product.isInStock()) {
                tvStock.setText(context.getString(R.string.in_stock, product.getQuantity()));
                tvStock.setTextColor(context.getColor(R.color.green));
            } else {
                tvStock.setText(R.string.out_of_stock);
                tvStock.setTextColor(context.getColor(R.color.red));
            }

            if (product.getPhotoUrl() != null && !product.getPhotoUrl().isEmpty()) {
                Glide.with(context)
                        .load(product.getPhotoUrl())
                        .apply(new RequestOptions().transform(new RoundedCorners(12)))
                        .placeholder(R.drawable.ic_product_placeholder)
                        .into(imgProduct);
            } else {
                imgProduct.setImageResource(R.drawable.ic_product_placeholder);
            }

            itemView.setOnClickListener(v -> listener.onProductClick(product));
        }
    }
}
