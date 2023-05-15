package com.example.photoeditor.ui.activities;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photoeditor.R;
import com.example.photoeditor.core.adapters.CollectionAdapter;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.photoeditor.core.classes.Constants.FINAL_PICTURE_DIRECTORY;
import static com.example.photoeditor.core.classes.Constants.REQUEST_CODE;

@SuppressWarnings("unused")
public class CollectionActivity extends AppCompatActivity {

    @BindView(R.id.recycler) RecyclerView recyclerView;
    private ArrayList<String> paths = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);
        ButterKnife.bind(this);

        scanFolder();

        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));

            final Drawable upArrow = getResources().getDrawable(R.drawable.ic_arrow_back);
            upArrow.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);
            getSupportActionBar().setHomeAsUpIndicator(upArrow);

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            Spannable title = new SpannableString(getString(R.string.my_collection));
            title.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.black)), 0, title.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            getSupportActionBar().setTitle(title);
        }
    }

    private void scanFolder(){

        File directory = new File(FINAL_PICTURE_DIRECTORY);
        File[] files = directory.listFiles();

        if (files == null) return;

        paths.clear();

        for (File file : files) {
            paths.add(file.getAbsolutePath());
        }

        loadItems();
    }

    private void loadItems(){
        CollectionAdapter adapter = new CollectionAdapter(this, paths, position -> {

            startActivityForResult(
                    new Intent(CollectionActivity.this, ImageViewerActivity.class)
                            .putStringArrayListExtra("paths", paths)
                            .putExtra("position", position), REQUEST_CODE);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        });

        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.collection_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.action_refresh){ scanFolder(); }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public boolean onSupportNavigateUp() {

        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE){ scanFolder(); }
    }
}
