package com.example.photoeditor.core.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.palette.graphics.Palette;
import androidx.viewpager.widget.PagerAdapter;
import com.example.photoeditor.R;

import com.ortiz.touchview.TouchImageView;

import java.util.ArrayList;

public class ImageAdapter extends PagerAdapter {

    private Activity activity;
    private ArrayList<String> paths;

    public ImageAdapter(Activity activity, ArrayList<String> paths) {
        this.activity = activity;
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
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (inflater == null) return new Object();

        View viewLayout = inflater.inflate(R.layout.layout_image_view, container, false);

        LinearLayout imgContainer = viewLayout.findViewById(R.id.img_container);
        TouchImageView touchImageView = viewLayout.findViewById(R.id.current_img);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(paths.get(position), options);

        Palette p = Palette.from(bitmap).generate();

        int mPaletteColor = p.getMutedColor(ContextCompat.getColor(activity, R.color.black));

        imgContainer.setBackgroundColor(mPaletteColor);

        touchImageView.setImageBitmap(bitmap);

        container.addView(viewLayout);

        return viewLayout;

    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((LinearLayout) object);
    }

}
