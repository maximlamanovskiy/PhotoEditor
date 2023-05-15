package com.example.photoeditor.core.libs.PhotoEditor.tools;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photoeditor.R;

import java.util.ArrayList;
import java.util.List;

import static com.example.photoeditor.core.classes.Constants.COLORS;

public class EditingToolsAdapter extends RecyclerView.Adapter<EditingToolsAdapter.ViewHolder> {

    private List<ToolModel> mToolList = new ArrayList<>();
    private OnItemSelected mOnItemSelected;
    private Context context;

    public EditingToolsAdapter(Context ctx, OnItemSelected onItemSelected) {
        context = ctx;
        mOnItemSelected = onItemSelected;
        mToolList.add(new ToolModel(context.getResources().getString(R.string.label_brush), R.drawable.ic_brush, ToolType.BRUSH));
        mToolList.add(new ToolModel(context.getResources().getString(R.string.label_text), R.drawable.ic_text, ToolType.TEXT));
        mToolList.add(new ToolModel(context.getResources().getString(R.string.label_eraser), R.drawable.ic_eraser, ToolType.ERASER));
        mToolList.add(new ToolModel(context.getResources().getString(R.string.label_filter), R.drawable.ic_photo_filter, ToolType.FILTER));
        mToolList.add(new ToolModel(context.getResources().getString(R.string.label_emoji), R.drawable.ic_insert_emoticon, ToolType.EMOJI));
        mToolList.add(new ToolModel(context.getResources().getString(R.string.label_sticker), R.drawable.ic_stars, ToolType.STICKER));
    }

    public interface OnItemSelected {
        void onToolSelected(ToolType toolType);
    }

    class ToolModel {
        private String mToolName;
        private int mToolIcon;
        private ToolType mToolType;

        ToolModel(String toolName, int toolIcon, ToolType toolType) {
            mToolName = toolName;
            mToolIcon = toolIcon;
            mToolType = toolType;
        }

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_row_editing_tools, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ToolModel item = mToolList.get(position);

        holder.txtTool.setText(item.mToolName);
        holder.txtTool.setTextColor(context.getResources().getColor(COLORS[position]));

        Drawable drawable = ContextCompat.getDrawable(context, item.mToolIcon);
        if (drawable == null){ return; }
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, ContextCompat.getColor(context, COLORS[position]));
        DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN);

        holder.imgToolIcon.setImageDrawable(drawable);
    }

    @Override
    public int getItemCount() {
        return mToolList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgToolIcon;
        TextView txtTool;

        ViewHolder(View itemView) {
            super(itemView);
            imgToolIcon = itemView.findViewById(R.id.imgToolIcon);
            txtTool = itemView.findViewById(R.id.txtTool);
            itemView.setOnClickListener(v -> mOnItemSelected.onToolSelected(mToolList.get(getLayoutPosition()).mToolType));
        }
    }
}
