package com.example.photoeditor.core.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.palette.graphics.Palette;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.photoeditor.R;

import com.google.firebase.storage.StorageReference;
import com.ortiz.touchview.TouchImageView;

import java.util.List;

public class ImageAdapter extends PagerAdapter {

    private Context context;
    private List<StorageReference> paths;

    public ImageAdapter(Context context, List<StorageReference> paths) {
        this.context = context;
        this.paths = paths;
    }

    @Override
    public int getCount() {
        return paths.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (inflater == null) return new Object();

        View viewLayout = inflater.inflate(R.layout.layout_image_view, container, false);

        LinearLayout imgContainer = viewLayout.findViewById(R.id.img_container);
        TouchImageView touchImageView = viewLayout.findViewById(R.id.current_img);

        StorageReference file = paths.get(position);

//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//        Bitmap bitmap = BitmapFactory.decodeFile(paths.get(position), options);

        Glide.with(context).asBitmap().load(file).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                touchImageView.setImageBitmap(resource);
                Palette p = Palette.from(resource).generate();
                int mPaletteColor = p.getMutedColor(ContextCompat.getColor(context, R.color.black));
                imgContainer.setBackgroundColor(mPaletteColor);
                container.addView(viewLayout);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
            }
        });
//        Glide.with(context).load(file).into(touchImageView);
//        container.addView(viewLayout);
        return viewLayout;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((LinearLayout) object);
    }

}
