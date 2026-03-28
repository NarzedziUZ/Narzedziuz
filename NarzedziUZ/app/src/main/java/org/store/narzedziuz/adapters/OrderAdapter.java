package org.store.narzedziuz.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.store.narzedziuz.R;
import org.store.narzedziuz.models.Order;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    private final Context context;
    private List<Order> orders;
    private final OnOrderClickListener listener;

    public OrderAdapter(Context context, OnOrderClickListener listener) {
        this.context = context;
        this.orders = new ArrayList<>();
        this.listener = listener;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        holder.bind(orders.get(position));
    }

    @Override
    public int getItemCount() { return orders.size(); }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvOrderId, tvDate, tvStatus, tvTotal, tvItemCount;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId   = itemView.findViewById(R.id.tv_order_id);
            tvDate      = itemView.findViewById(R.id.tv_date);
            tvStatus    = itemView.findViewById(R.id.tv_status);
            tvTotal     = itemView.findViewById(R.id.tv_total);
            tvItemCount = itemView.findViewById(R.id.tv_item_count);
        }

        void bind(Order order) {
            tvOrderId.setText(context.getString(R.string.order_number, order.getId().substring(0, Math.min(8, order.getId().length()))));
            tvDate.setText(order.getFormattedDate());
            tvStatus.setText(order.getStatus());
            tvTotal.setText(String.format(Locale.getDefault(), "%.2f PLN", order.getTotalPrice()));
            int count = order.getItems() != null ? order.getItems().size() : 0;
            tvItemCount.setText(context.getResources().getQuantityString(R.plurals.item_count, count, count));
            itemView.setOnClickListener(v -> listener.onOrderClick(order));
        }
    }
}
