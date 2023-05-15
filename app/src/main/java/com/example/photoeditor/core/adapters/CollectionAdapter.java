package com.example.photoeditor.core.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photoeditor.R;

import java.io.File;
import java.util.ArrayList;

public class CollectionAdapter extends RecyclerView.Adapter<CollectionAdapter.ViewHolder> {

    private Context context;
    private ArrayList<String> paths;
    private ItemClickListener itemClickListener;

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView itemPreview;

        ViewHolder(View itemView) {
            super(itemView);

            itemPreview = itemView.findViewById(R.id.item_preview);
        }

    }

    public CollectionAdapter(Context context, ArrayList<String> paths, ItemClickListener listener) {

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
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_collection_item, parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String path = paths.get(position);

        holder.itemPreview.setOnClickListener(view -> itemClickListener.onItemClick(position));

        holder.itemPreview.setImageURI(FileProvider.getUriForFile(context,context.getPackageName() + ".provider", new File(path)));

    }

    public interface ItemClickListener {
        void onItemClick(int position);
    }
}
