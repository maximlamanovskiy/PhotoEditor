package com.example.photoeditor.ui.activities;


import static com.example.photoeditor.core.classes.Constants.FINAL_PICTURE_DIRECTORY;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.photoeditor.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ortiz.touchview.TouchImageView;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

@SuppressWarnings("unused")
public class ImageSharingActivity extends AppCompatActivity {

    private Uri uri;
    //    private File file;
    @BindView(R.id.preview)
    TouchImageView preview;

    private final static String PACKAGE_WHATSAPP = "com.whatsapp";
    private final static String PACKAGE_WHATSAPP_4_B = "com.whatsapp.w4b";
    private final static String PACKAGE_INSTAGRAM = "com.instagram.android";
    private final static String PACKAGE_TWITTER = "com.twitter.android";
    private final static String PACKAGE_TWITTER_LITE = "com.twitter.android.lite";
    private final static String PACKAGE_SNAPCHAT = "com.snapchat.android";
    private final static String PACKAGE_FACEBOOK = "com.facebook.katana";
    private final static String PACKAGE_FACEBOOK_LITE = "com.facebook.lite";

    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_sharing);
        ButterKnife.bind(this);

        if (getIntent() != null && getIntent().hasExtra("path")) {

            String path = getIntent().getStringExtra("path");

            if (path == null) return;

            File storageDir = new File(FINAL_PICTURE_DIRECTORY);
            boolean success = true;
            if (!storageDir.exists()) {
                success = storageDir.mkdirs();
            }
            if (!success) return;
            File file = new File(storageDir, path);
            try {
                if (!file.createNewFile()) return;
            } catch (IOException e) {
                return;
            }

            StorageReference storageRef = storage.getReference();
            StorageReference fileRef = storageRef.child(user.getUid()).child(path);
            Glide.with(this).load(fileRef).into(preview);
            fileRef.getFile(file)
                    .addOnSuccessListener(taskSnapshot -> uri = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", file))
                    .addOnFailureListener(exception -> {
                        // Handle any errors
                    });
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));

            final Drawable upArrow = getResources().getDrawable(R.drawable.ic_arrow_back);
            upArrow.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);
            getSupportActionBar().setHomeAsUpIndicator(upArrow);

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            Spannable title = new SpannableString(getString(R.string.share));
            title.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.black)), 0, title.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            getSupportActionBar().setTitle(title);
        }
    }

    @OnClick(R.id.whatsapp)
    void shareOnWhatsapp() {
        shareOperator(PACKAGE_WHATSAPP, "Whatsapp");
    }

    @OnClick(R.id.facebook)
    void shareOnFacebook() {
        shareOperator(PACKAGE_FACEBOOK, "Facebook");
    }

    @OnClick(R.id.instagram)
    void shareOnInsta() {
        shareOperator(PACKAGE_INSTAGRAM, "Instagram");
    }

    @OnClick(R.id.twitter)
    void shareOnTwitter() {
        shareOperator(PACKAGE_TWITTER, "Twitter");
    }

    @OnClick(R.id.snapchat)
    void shareOnSnapchat() {
        shareOperator(PACKAGE_SNAPCHAT, "Snapchat");
    }

    @OnClick(R.id.share)
    void share() {

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.setType("image/*");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));

    }

    private void shareOperator(String packageName, String socialMedia) {

        if (uri == null) return;

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.setPackage(packageName);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {

            startActivity(intent);

        } catch (android.content.ActivityNotFoundException ex) {

            switch (packageName) {

                case PACKAGE_TWITTER:
                    shareOperator(PACKAGE_TWITTER_LITE, "Twitter");
                    break;

                case PACKAGE_WHATSAPP:
                    shareOperator(PACKAGE_WHATSAPP_4_B, "Whatsapp");
                    break;

                case PACKAGE_FACEBOOK:
                    shareOperator(PACKAGE_FACEBOOK_LITE, "Facebook");
                    break;

                default:
                    Toast.makeText(this, socialMedia + "\t" + getString(R.string.not_installed), Toast.LENGTH_SHORT).show();

            }

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.image_sharing_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.action_home) {

            startActivity(
                    new Intent(this, MainActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
            );
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        }

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
}
