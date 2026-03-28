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
import org.store.narzedziuz.models.CartItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    public interface OnCartItemListener {
        void onRemove(CartItem item, int position);
    }

    private final Context context;
    private List<CartItem> items;
    private final OnCartItemListener listener;

    public CartAdapter(Context context, OnCartItemListener listener) {
        this.context = context;
        this.items = new ArrayList<>();
        this.listener = listener;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public List<CartItem> getItems() { return items; }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        holder.bind(items.get(position), position);
    }

    @Override
    public int getItemCount() { return items.size(); }

    class CartViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgProduct;
        private final TextView tvName, tvManufacturer, tvQuantity, tvPrice;
        private final ImageButton btnRemove;

        CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct    = itemView.findViewById(R.id.img_product);
            tvName        = itemView.findViewById(R.id.tv_product_name);
            tvManufacturer= itemView.findViewById(R.id.tv_manufacturer);
            tvQuantity    = itemView.findViewById(R.id.tv_quantity);
            tvPrice       = itemView.findViewById(R.id.tv_price);
            btnRemove     = itemView.findViewById(R.id.btn_remove);
        }

        void bind(CartItem item, int position) {
            if (item.getProduct() != null) {
                tvName.setText(item.getProduct().getName());
                tvManufacturer.setText(item.getProduct().getManufacturer());
                if (item.getProduct().getPhotoUrl() != null && !item.getProduct().getPhotoUrl().isEmpty()) {
                    Glide.with(context).load(item.getProduct().getPhotoUrl())
                            .placeholder(R.drawable.ic_product_placeholder).into(imgProduct);
                } else {
                    imgProduct.setImageResource(R.drawable.ic_product_placeholder);
                }
            }
            tvQuantity.setText(context.getString(R.string.quantity_label, item.getQuantity()));
            tvPrice.setText(String.format(Locale.getDefault(), "%.2f PLN", item.getTotalPrice()));
            btnRemove.setOnClickListener(v -> listener.onRemove(item, position));
        }
    }
}
