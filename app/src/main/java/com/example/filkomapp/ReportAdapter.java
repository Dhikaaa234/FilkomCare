package com.example.filkomapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {
    private Context context;
    private List<Report> reports;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Report report);
        void onLikeClick(Report report, boolean isLiked);
    }

    public ReportAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.reports = new ArrayList<>();
    }

    public void setReports(List<Report> reports) {
        this.reports = reports;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.news_item, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Report report = reports.get(position);

        holder.titleTextView.setText(report.getTitle());
        holder.locationTextView.setText(report.getLocation());
        holder.statusTextView.setText(report.getStatus());
        holder.likesTextView.setText(String.valueOf(report.getLikes()));

        if (report.getImage() != null && !report.getImage().isEmpty()) {
            Bitmap bitmap = FirebaseHelper.base64ToBitmap(report.getImage());
            holder.reportImageView.setImageBitmap(bitmap);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(report);
            }
        });

        holder.likeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isLiked = report.getLikedBy() != null &&
                        report.getLikedBy().containsKey(FirebaseAuth.getInstance().getCurrentUser().getUid());
                listener.onLikeClick(report, isLiked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        ImageView reportImageView;
        TextView titleTextView, locationTextView, statusTextView, likesTextView;
        View likeLayout;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            reportImageView = itemView.findViewById(R.id.reportImage);
            titleTextView = itemView.findViewById(R.id.title);
            locationTextView = itemView.findViewById(R.id.place);
            statusTextView = itemView.findViewById(R.id.statusText);
            likesTextView = itemView.findViewById(R.id.likesCount);
            likeLayout = itemView.findViewById(R.id.like);
        }
    }
}