package com.yourname.wanderlust.ui.results;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yourname.wanderlust.R;
import com.yourname.wanderlust.data.model.TripResult;

import java.util.ArrayList;
import java.util.List;


public class TripResultAdapter extends RecyclerView.Adapter<TripResultAdapter.ViewHolder> {

    private List<TripResult> items = new ArrayList<>();
    public void setItems(List<TripResult> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trip_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TripResult r = items.get(position);

        if (r.estimatedCost != null && !r.estimatedCost.isEmpty()) {
            holder.tvEstimatedCost.setText("💰 " + r.estimatedCost);
            holder.tvEstimatedCost.setVisibility(View.VISIBLE);
        }
        holder.bind(items.get(position));

        holder.itemView.setOnClickListener(v ->

        {
            if (listener != null) listener.onItemClick(items.get(position));

        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvBadge, tvDescription, tvHighlights,
                tvDuration, tvDifficulty, tvRating, tvSource, tvEstimatedCost;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvBadge = itemView.findViewById(R.id.tvBadge);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvHighlights = itemView.findViewById(R.id.tvHighlights);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvDifficulty = itemView.findViewById(R.id.tvDifficulty);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvSource = itemView.findViewById(R.id.tvSource);

            tvEstimatedCost = itemView.findViewById(R.id.tvEstimatedCost);
        }

        void bind(TripResult r) {
            tvTitle.setText(r.title);
            tvBadge.setText(r.badge);
            tvDescription.setText(r.description);
            tvDuration.setText("⏱ " + r.duration);
            tvDifficulty.setText("🥾 " + r.difficulty);
            tvRating.setText("★ " + r.rating);
            tvSource.setText("📡 " + r.source);

            if (r.highlights != null && !r.highlights.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (String h : r.highlights) {
                    sb.append("• ").append(h).append("\n");
                }
                tvHighlights.setText(sb.toString().trim());
            }
        }
    }

    // הוסף interface
    public interface OnItemClickListener {
        void onItemClick(TripResult item);
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}