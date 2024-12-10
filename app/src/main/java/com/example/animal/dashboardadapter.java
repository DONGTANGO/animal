package com.example.animal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
public class dashboardadapter extends RecyclerView.Adapter<dashboardadapter.ViewHolder> {

    private List<String> items;
    private OnItemClickListener listener;
    private String currentCategory; // 현재 카테고리 저장



    public interface OnItemClickListener {
        void onItemClick(String category,String title);
    }

    public dashboardadapter(List<String> items, String currentCategory,OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
        this.currentCategory = currentCategory; // 카테고리 저장


    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textView.setText(items.get(position));
        String title = items.get(position);


        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(currentCategory, title); // 카테고리와 제목 전달
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}