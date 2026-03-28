package org.store.narzedziuz.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.store.narzedziuz.R;
import org.store.narzedziuz.models.Review;

import java.util.ArrayList;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private final Context context;
    private List<Review> reviews;

    public ReviewAdapter(Context context) {
        this.context = context;
        this.reviews = new ArrayList<>();
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        holder.bind(reviews.get(position));
    }

    @Override
    public int getItemCount() { return reviews.size(); }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvUserName, tvDate, tvComment;
        private final RatingBar ratingBar;

        ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvDate     = itemView.findViewById(R.id.tv_date);
            tvComment  = itemView.findViewById(R.id.tv_comment);
            ratingBar  = itemView.findViewById(R.id.rating_bar);
        }

        void bind(Review review) {
            tvUserName.setText(review.getUserName());
            tvDate.setText(review.getFormattedDate());
            tvComment.setText(review.getComment() != null ? review.getComment() : "");
            ratingBar.setRating(review.getRating());
        }
    }
}
