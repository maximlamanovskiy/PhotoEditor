package com.example.photoeditor.core.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.photoeditor.R;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CollectionAdapter extends RecyclerView.Adapter<CollectionAdapter.ViewHolder> {

    private Context context;
    private List<StorageReference> paths;
    private ItemClickListener itemClickListener;

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView itemPreview;

        ViewHolder(View itemView) {
            super(itemView);

            itemPreview = itemView.findViewById(R.id.item_preview);
        }

    }

    public CollectionAdapter(Context context, List<StorageReference> paths, ItemClickListener listener) {

        this.itemClickListener = listener;
        this.context = context;
        this.paths = paths;

    }

    @Override
    public int getItemCount() {
        return paths.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_collection_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StorageReference path = paths.get(position);

        holder.itemPreview.setOnClickListener(view -> itemClickListener.onItemClick(position));

        Glide.with(context).load(path).into(holder.itemPreview);
    }

    public interface ItemClickListener {
        void onItemClick(int position);
    }
}
