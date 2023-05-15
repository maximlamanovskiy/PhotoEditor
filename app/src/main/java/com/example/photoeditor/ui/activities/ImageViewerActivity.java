package com.example.photoeditor.ui.activities;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.appbar.AppBarLayout;
import com.example.photoeditor.R;
import com.example.photoeditor.core.adapters.ImageAdapter;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.photoeditor.core.classes.Constants.EMPTY;

public class ImageViewerActivity extends AppCompatActivity {

    @BindView(R.id.app_bar_layout) AppBarLayout appBarLayout;

    @BindView(R.id.viewPager) ViewPager viewPager;

    @BindView(R.id.toolbar) Toolbar toolbar;

    ArrayList<String> paths = new ArrayList<>();
    ImageAdapter adapter;
    int currentPosition;
    int initPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        ButterKnife.bind(this);

        appBarLayout.bringToFront();
        appBarLayout.invalidate();

        setSupportActionBar(toolbar);

        if (getIntent() != null){

            paths = getIntent().getStringArrayListExtra("paths");
            currentPosition = initPosition = getIntent().getIntExtra("position", 0);

            adapter = new ImageAdapter(this, paths);

            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(initPosition);

            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    currentPosition = position;
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }

        if (getSupportActionBar() != null) {

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(EMPTY);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.image_viewer_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){

            case R.id.action_delete:

                new AlertDialog.Builder(this)
                        .setTitle(R.string.confirmation)
                        .setMessage(R.string.delete_image)
                        .setPositiveButton(R.string.yes, (dialog, which) -> deleteItem())
                        .setNegativeButton(R.string.cancel, null)
                        .show();

                break;

            case R.id.action_share:

                Uri uri = FileProvider.getUriForFile(this,this.getApplicationContext().getPackageName() + ".provider", new File(paths.get(currentPosition)));

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.setType("image/*");
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));

                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteItem(){

        File file = new File(paths.get(currentPosition));

        if (paths.size() > 1){

            if (file.delete()){

                viewPager.setAdapter(null);
                paths.remove(currentPosition);

                adapter = new ImageAdapter(this, paths);
                viewPager.setAdapter(adapter);

                adapter.notifyDataSetChanged();
                currentPosition -= 1;

                viewPager.setCurrentItem(currentPosition);

            }else {
                Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
            }

        }else {

            if (file.delete()){

                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

            }else {
                Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
            }

        }

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

}
