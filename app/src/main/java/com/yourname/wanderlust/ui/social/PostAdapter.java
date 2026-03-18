package com.yourname.wanderlust.ui.social;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.yourname.wanderlust.R;
import com.yourname.wanderlust.data.model.FirebasePost;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    public interface OnPostActionListener {
        void onToggleLike(String postId, boolean isLike);
    }

    private List<FirebasePost> items = new ArrayList<>();
    private OnPostActionListener listener;
    private String currentUserId = "";

    public void setItems(List<FirebasePost> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public void setCurrentUserId(String uid) {
        this.currentUserId = uid;
    }

    public void setOnPostActionListener(OnPostActionListener l) {
        this.listener = l;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FirebasePost post = items.get(position);

        // מידע בסיסי
        String initials = post.username != null && !post.username.isEmpty()
                ? String.valueOf(post.username.charAt(0)).toUpperCase() : "?";
        holder.tvAvatar.setText(initials);
        holder.tvUsername.setText(post.username);
        holder.tvPostLocation.setText("📍 " + post.location);
        holder.tvContent.setText(post.content);

        // דירוג
        holder.ratingBar.setRating(post.rating);
        // לייק / דיסלייק
        boolean liked    = post.likedBy    != null && post.likedBy.contains(currentUserId);
        boolean disliked = post.dislikedBy != null && post.dislikedBy.contains(currentUserId);

        // עדכן טקסט
        holder.btnLike.setText("👍 " + post.likes);
        holder.btnDislike.setText("👎 " + post.dislikes);

        // עדכן צבע לפי מצב
        int green  = android.graphics.Color.parseColor("#1D9E75");
        int red    = android.graphics.Color.parseColor("#E24B4A");
        int grey   = android.graphics.Color.parseColor("#888888");

        holder.btnLike.setTextColor(liked ? green : grey);
        holder.btnDislike.setTextColor(disliked ? red : grey);

        // לחיצה — לייק
        holder.btnLike.setOnClickListener(v -> {
            if (listener != null) listener.onToggleLike(post.id, true);
        });

        // לחיצה — דיסלייק
        holder.btnDislike.setOnClickListener(v -> {
            if (listener != null) listener.onToggleLike(post.id, false);
        });

        // זמן
        SimpleDateFormat sdf = new SimpleDateFormat(
                "dd/MM/yyyy HH:mm", Locale.getDefault());
        holder.tvTime.setText(sdf.format(new Date(post.timestamp)));

        // סנטימנט
        // קבע סנטימנט משולב — AI + דירוג
        String sentiment = post.sentiment != null ? post.sentiment : "neutral";
        int stars = (int) post.rating;

        String sentimentText;
        int bgResource;
        String textColor;

// שלב דירוג עם סנטימנט AI
        if (stars == 5 && sentiment.equals("positive")) {
            sentimentText = "🤩 חוויה מושלמת";
            bgResource = R.drawable.badge_positive;
            textColor = "#085041";
        } else if (stars >= 4 && sentiment.equals("positive")) {
            sentimentText = "😊 ממליץ בחום";
            bgResource = R.drawable.badge_positive;
            textColor = "#085041";
        } else if (stars >= 4 && sentiment.equals("neutral")) {
            sentimentText = "🙂 חוויה טובה";
            bgResource = R.drawable.badge_positive;
            textColor = "#085041";
        } else if (stars == 3 && sentiment.equals("positive")) {
            sentimentText = "👍 שווה ביקור";
            bgResource = R.drawable.badge_neutral;
            textColor = "#444444";
        } else if (stars == 3 && sentiment.equals("neutral")) {
            sentimentText = "😐 רגשות מעורבים";
            bgResource = R.drawable.badge_neutral;
            textColor = "#444444";
        } else if (stars == 3 && sentiment.equals("negative")) {
            sentimentText = "🤔 לא בטוח";
            bgResource = R.drawable.badge_neutral;
            textColor = "#444444";
        } else if (stars == 2 && sentiment.equals("negative")) {
            sentimentText = "😕 מאכזב";
            bgResource = R.drawable.badge_negative;
            textColor = "#4A1B0C";
        } else if (stars == 2 && sentiment.equals("neutral")) {
            sentimentText = "😑 מתחת לציפיות";
            bgResource = R.drawable.badge_neutral;
            textColor = "#444444";
        } else if (stars <= 1 && sentiment.equals("negative")) {
            sentimentText = "👎 לא מומלץ";
            bgResource = R.drawable.badge_negative;
            textColor = "#4A1B0C";
        } else if (stars <= 1) {
            sentimentText = "😞 חוויה גרועה";
            bgResource = R.drawable.badge_negative;
            textColor = "#4A1B0C";
        } else if (sentiment.equals("positive")) {
            sentimentText = "😊 חיובי";
            bgResource = R.drawable.badge_positive;
            textColor = "#085041";
        } else if (sentiment.equals("negative")) {
            sentimentText = "😕 שלילי";
            bgResource = R.drawable.badge_negative;
            textColor = "#4A1B0C";
        } else {
            sentimentText = "😐 ספק";
            bgResource = R.drawable.badge_neutral;
            textColor = "#444444";
        }

        holder.tvSentiment.setText(sentimentText);
        holder.tvSentiment.setBackgroundResource(bgResource);
        holder.tvSentiment.setTextColor(android.graphics.Color.parseColor(textColor));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvUsername, tvPostLocation,
                tvContent, tvSentiment, tvTime;
        RatingBar ratingBar;
        MaterialButton btnLike, btnDislike;

        ViewHolder(View itemView) {
            super(itemView);
            tvAvatar       = itemView.findViewById(R.id.tvAvatar);
            tvUsername     = itemView.findViewById(R.id.tvUsername);
            tvPostLocation = itemView.findViewById(R.id.tvPostLocation);
            tvContent      = itemView.findViewById(R.id.tvContent);
            tvSentiment    = itemView.findViewById(R.id.tvSentiment);
            tvTime         = itemView.findViewById(R.id.tvTime);
            ratingBar      = itemView.findViewById(R.id.ratingBarPost);
            btnLike        = itemView.findViewById(R.id.btnLike);
            btnDislike     = itemView.findViewById(R.id.btnDislike);
        }
    }
}