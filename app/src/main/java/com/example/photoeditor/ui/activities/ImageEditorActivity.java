package com.example.photoeditor.ui.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.ChangeBounds;
import androidx.transition.TransitionManager;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.example.photoeditor.R;
import com.example.photoeditor.core.libs.PhotoEditor.EmojiBSFragment;
import com.example.photoeditor.core.libs.PhotoEditor.PropertiesBSFragment;
import com.example.photoeditor.core.libs.PhotoEditor.StickerBSFragment;
import com.example.photoeditor.core.libs.PhotoEditor.TextEditorDialogFragment;
import com.example.photoeditor.core.libs.PhotoEditor.base.BaseActivity;
import com.example.photoeditor.core.libs.PhotoEditor.filters.FilterListener;
import com.example.photoeditor.core.libs.PhotoEditor.filters.FilterViewAdapter;
import com.example.photoeditor.core.libs.PhotoEditor.tools.EditingToolsAdapter;
import com.example.photoeditor.core.libs.PhotoEditor.tools.ToolType;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ja.burhanrashid52.photoeditor.OnPhotoEditorListener;
import ja.burhanrashid52.photoeditor.OnSaveBitmap;
import ja.burhanrashid52.photoeditor.PhotoEditor;
import ja.burhanrashid52.photoeditor.PhotoEditorView;
import ja.burhanrashid52.photoeditor.PhotoFilter;
import ja.burhanrashid52.photoeditor.SaveSettings;
import ja.burhanrashid52.photoeditor.TextStyleBuilder;
import ja.burhanrashid52.photoeditor.ViewType;

import static com.example.photoeditor.core.classes.AppUtilities.getPathFromUri;
import static com.example.photoeditor.core.classes.Constants.FINAL_PICTURE_DIRECTORY;
import static com.example.photoeditor.core.classes.Constants.TAGS_NAME;
import static com.example.photoeditor.core.classes.Constants.TEMP_PICTURE_DIRECTORY;

@SuppressWarnings("unused")
public class ImageEditorActivity extends BaseActivity implements OnPhotoEditorListener,
        PropertiesBSFragment.Properties,
        EmojiBSFragment.EmojiListener,
        StickerBSFragment.StickerListener, EditingToolsAdapter.OnItemSelected, FilterListener {

    private static final String TAG = ImageEditorActivity.class.getSimpleName();
    private static final int CAMERA_REQUEST = 0;
    private static final int PICK_REQUEST = 1;
    private static final int CROP_REQUEST = 2;
    private static final int INFO_REQUEST = 3;

    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private PhotoEditor mPhotoEditor;
    private PropertiesBSFragment mPropertiesBSFragment;
    private EmojiBSFragment mEmojiBSFragment;
    private StickerBSFragment mStickerBSFragment;
    private ConstraintSet mConstraintSet;
    private boolean mIsFilterVisible;
    private String currentImgPath;
    private Uri currentImgUri;

    @BindView(R.id.photoEditorView)
    PhotoEditorView mPhotoEditorView;
    @BindView(R.id.txtCurrentTool)
    TextView mTxtCurrentTool;
    @BindView(R.id.rvConstraintTools)
    RecyclerView mRvTools;
    @BindView(R.id.rvFilterView)
    RecyclerView mRvFilters;
    @BindView(R.id.rootView)
    ConstraintLayout mRootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_editor);
        ButterKnife.bind(this);

        EditingToolsAdapter mEditingToolsAdapter = new EditingToolsAdapter(this, this);
        FilterViewAdapter mFilterViewAdapter = new FilterViewAdapter(this);

        mConstraintSet = new ConstraintSet();
        mPropertiesBSFragment = new PropertiesBSFragment();
        mEmojiBSFragment = new EmojiBSFragment();
        mStickerBSFragment = new StickerBSFragment();
        mStickerBSFragment.setStickerListener(this);
        mEmojiBSFragment.setEmojiListener(this);
        mPropertiesBSFragment.setPropertiesChangeListener(this);

        LinearLayoutManager llmTools = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRvTools.setLayoutManager(llmTools);
        mRvTools.setAdapter(mEditingToolsAdapter);

        LinearLayoutManager llmFilters = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRvFilters.setLayoutManager(llmFilters);
        mRvFilters.setAdapter(mFilterViewAdapter);


        Typeface mTextRobotoTf = Typeface.createFromAsset(getAssets(), "fonts/roboto-medium.ttf");
        Typeface mEmojiTypeFace = Typeface.createFromAsset(getAssets(), "fonts/emojione-android.ttf");

        mPhotoEditor = new PhotoEditor.Builder(this, mPhotoEditorView)
                .setPinchTextScalable(true)
                .setDefaultTextTypeface(mTextRobotoTf)
                .setDefaultEmojiTypeface(mEmojiTypeFace)
                .build();

        mPhotoEditor.setOnPhotoEditorListener(this);

        getUriIntent();
    }

    private void getUriIntent() {

        if (getIntent() != null && getIntent().hasExtra("uriStr")) {

            mPhotoEditor.clearAllViews();
            String uriStr = getIntent().getStringExtra("uriStr");
            currentImgUri = Uri.parse(uriStr);

            mPhotoEditorView.getSource().setImageURI(currentImgUri);

        }

    }

    @Override
    public void onEditTextChangeListener(final View rootView, String text, int colorCode) {
        TextEditorDialogFragment textEditorDialogFragment =
                TextEditorDialogFragment.show(this, text, colorCode);
        textEditorDialogFragment.setOnTextEditorListener((inputText, colorCode1) -> {
            final TextStyleBuilder styleBuilder = new TextStyleBuilder();
            styleBuilder.withTextColor(colorCode1);

            mPhotoEditor.editText(rootView, inputText, styleBuilder);
            mTxtCurrentTool.setText(R.string.label_text);
        });
    }

    @Override
    public void onAddViewListener(ViewType viewType, int numberOfAddedViews) {
        Log.d(TAG, "onAddViewListener() called with: viewType = [" + viewType + "], numberOfAddedViews = [" + numberOfAddedViews + "]");
    }

    @Override
    public void onRemoveViewListener(ViewType viewType, int numberOfAddedViews) {
        Log.d(TAG, "onRemoveViewListener() called with: viewType = [" + viewType + "], numberOfAddedViews = [" + numberOfAddedViews + "]");
    }

    @Override
    public void onStartViewChangeListener(ViewType viewType) {
        Log.d(TAG, "onStartViewChangeListener() called with: viewType = [" + viewType + "]");
    }

    @Override
    public void onStopViewChangeListener(ViewType viewType) {
        Log.d(TAG, "onStopViewChangeListener() called with: viewType = [" + viewType + "]");
    }

    @OnClick(R.id.imgUndo)
    void onImgUndoClick() {
        mPhotoEditor.undo();
    }

    @OnClick(R.id.imgRedo)
    void onImgRedoClick() {
        mPhotoEditor.redo();
    }

    @OnClick(R.id.imgSave)
    void onImgSaveClick() {
        saveImage();
    }

    @OnClick(R.id.imgClose)
    void onImgCloseClick() {
        onBackPressed();
    }

    @OnClick(R.id.imgCamera)
    void onImgCameraClick() {
        boolean success = true;
        File storageDir = new File(TEMP_PICTURE_DIRECTORY);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (!storageDir.exists()) {
            success = storageDir.mkdirs();
        }

        if (success) {
            File file = new File(storageDir, "temp-original.jpg");
            currentImgPath = file.getPath();
            currentImgUri = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", file);
            cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, currentImgUri);
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
        }
    }

    @OnClick(R.id.imgGallery)
    void onImgGalleryClick() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)), PICK_REQUEST);
    }

    @OnClick(R.id.imgCrop)
    void onImgCropClick() {

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        }, CROP_REQUEST);

    }

    @OnClick(R.id.imgInfo)
    void onImgInfoClick() {

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, INFO_REQUEST);

    }

    @SuppressLint("MissingPermission")
    private void saveImage() {
//        if (requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//            showLoading();
//
//            boolean success = true;
//            File storageDir = new File(FINAL_PICTURE_DIRECTORY);
//
//            if (!storageDir.exists()){success = storageDir.mkdirs();}
//
//            if (!success) return;
//
//            File file = new File(storageDir, System.currentTimeMillis() + ".png");
//            try {
//                if (!file.createNewFile()) return;
//
//                SaveSettings saveSettings = new SaveSettings.Builder()
//                        .setClearViewsEnabled(true)
//                        .setTransparencyEnabled(true)
//                        .build();
//
//                mPhotoEditor.saveAsFile(file.getAbsolutePath(), saveSettings, new PhotoEditor.OnSaveListener() {
//                    @Override
//                    public void onSuccess(@NonNull String imagePath) {
//
//                        hideLoading();
//                        showSnackbar(getString(R.string.image_saving_success));
//                        currentImgUri = Uri.fromFile(new File(imagePath));
//
//                        mPhotoEditorView.getSource().setImageURI(currentImgUri);
//
//                        startActivity(new Intent(ImageEditorActivity.this, ImageSharingActivity.class).putExtra("path", imagePath));
//                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
//                    }
//
//                    @Override
//                    public void onFailure(@NonNull Exception exception) {
//                        hideLoading();
//                        showSnackbar(getString(R.string.image_saving_failure));
//                    }
//                });
//
//            } catch (IOException e) {
//
//                e.printStackTrace();
//                hideLoading();
//
//                if (e.getMessage() != null) showSnackbar(e.getMessage());
//
//            }
//        }
        showLoading();

        String fileName = System.currentTimeMillis() + ".png";
        // Creating link to file
        StorageReference storageRef = storage.getReference();
        StorageReference fileRef = storageRef.child(user.getUid()).child(fileName);

        SaveSettings saveSettings = new SaveSettings.Builder()
                .setClearViewsEnabled(true)
                .setTransparencyEnabled(true)
                .build();

        mPhotoEditor.saveAsBitmap(saveSettings, new OnSaveBitmap() {
            @Override
            public void onBitmapReady(Bitmap saveBitmap) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                saveBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] data = baos.toByteArray();

                UploadTask uploadTask = fileRef.putBytes(data);
                uploadTask.continueWithTask((Continuation<UploadTask.TaskSnapshot, Task<Uri>>) task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return fileRef.getDownloadUrl();
                }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {
                    if (task.isSuccessful()) {
                        mPhotoEditorView.getSource().setImageBitmap(saveBitmap);
                        hideLoading();
                        startActivity(new Intent(ImageEditorActivity.this, ImageSharingActivity.class).putExtra("path", fileName));
                    } else {
                        hideLoading();
                        showSnackbar(getString(R.string.image_saving_failure));
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                hideLoading();
                showSnackbar(getString(R.string.image_saving_failure));
            }
        });
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {

            switch (requestCode) {

                case CAMERA_REQUEST:

                    boolean success = true;
                    File storageDir = new File(TEMP_PICTURE_DIRECTORY);

                    if (!storageDir.exists()) {
                        success = storageDir.mkdirs();
                    }

                    if (success) {
                        File file = new File(storageDir, "temp-original.jpg");
                        currentImgPath = file.getPath();
                        currentImgUri = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", file);
                    }

                    try {

                        mPhotoEditor.clearAllViews();
                        Bitmap cameraBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), currentImgUri);
                        mPhotoEditorView.getSource().setImageBitmap(cameraBitmap);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    break;

                case PICK_REQUEST:

                    try {

                        mPhotoEditor.clearAllViews();
                        currentImgUri = data.getData();
                        Bitmap galleryBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), currentImgUri);
                        mPhotoEditorView.getSource().setImageBitmap(galleryBitmap);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    break;

                case UCrop.REQUEST_CROP:

                    Uri resultUri = UCrop.getOutput(data);
                    if (resultUri != null) {

                        currentImgUri = resultUri;

                        try {

                            mPhotoEditor.clearAllViews();
                            Bitmap galleryBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), currentImgUri);
                            mPhotoEditorView.getSource().setImageBitmap(galleryBitmap);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }

                    break;
            }

        } else if (resultCode == UCrop.RESULT_ERROR) {

            final Throwable cropError = UCrop.getError(data);
            if (cropError == null) return;
            cropError.printStackTrace();

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0) {

            switch (requestCode) {

                case CROP_REQUEST:
                    boolean pass = true;
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            pass = false;
                        }
                    }

                    if (pass) {
                        openImageCropper();
                    } else {
                        Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                    }
                    break;

                case INFO_REQUEST:

                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        extractEXIFData();
                    }

                    break;
            }

        } else {
            Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onColorChanged(int colorCode) {
        mPhotoEditor.setBrushColor(colorCode);
        mTxtCurrentTool.setText(R.string.label_brush);
    }

    @Override
    public void onOpacityChanged(int opacity) {
        mPhotoEditor.setOpacity(opacity);
        mTxtCurrentTool.setText(R.string.label_brush);
    }

    @Override
    public void onBrushSizeChanged(int brushSize) {
        mPhotoEditor.setBrushSize(brushSize);
        mTxtCurrentTool.setText(R.string.label_brush);
    }

    @Override
    public void onEmojiClick(String emojiUnicode) {
        mPhotoEditor.addEmoji(emojiUnicode);
        mTxtCurrentTool.setText(R.string.label_emoji);
    }

    @Override
    public void onStickerClick(Bitmap bitmap) {
        mPhotoEditor.addImage(bitmap);
        mTxtCurrentTool.setText(R.string.label_sticker);
    }

    @Override
    public void isPermissionGranted(boolean isGranted, String permission) {
        if (isGranted) {
            saveImage();
        }
    }

    private void openImageCropper() {

        UCrop.Options options = new UCrop.Options();

        options.setLogoColor(getResources().getColor(R.color.black));
        options.setToolbarColor(getResources().getColor(R.color.white));
        options.setStatusBarColor(getResources().getColor(R.color.white));
        options.setActiveWidgetColor(getResources().getColor(R.color.black));
        options.setToolbarWidgetColor(getResources().getColor(R.color.black));
        options.setActiveControlsWidgetColor(getResources().getColor(R.color.white));

        options.setCropGridRowCount(2);
        options.setCropGridColumnCount(2);
        options.setFreeStyleCropEnabled(true);
        options.setToolbarTitle(getResources().getString(R.string.resize));

        boolean success = true;
        File storageDir = new File(TEMP_PICTURE_DIRECTORY);

        if (!storageDir.exists()) {
            success = storageDir.mkdirs();
        }

        if (success) {
            File file = new File(storageDir, "temp-cropped.jpg");
            try {

                boolean result = file.createNewFile();

                Uri uri = Uri.parse(file.toURI().toString());

                UCrop
                        .of(currentImgUri, uri)
                        .withOptions(options)
                        .start(this);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    private void extractEXIFData() {

        try {

            String path = getPathFromUri(ImageEditorActivity.this, currentImgUri);

            if (path == null) {
                return;
            }

            showInfoDialog(ImageMetadataReader.readMetadata(new File(path)));

        } catch (Exception e1) {
            e1.printStackTrace();

            if (currentImgPath == null) {
                Toast.makeText(this, R.string.exif_extract_error, Toast.LENGTH_SHORT).show();
                return;
            }

            try {

                showInfoDialog(ImageMetadataReader.readMetadata(new File(currentImgPath)));

            } catch (Exception e2) {
                e2.printStackTrace();
            }

        }

    }

    private void showSaveDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.save_dialog_msg));
        builder.setPositiveButton(getString(R.string.save), (dialog, which) -> saveImage());
        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss());
        builder.setNeutralButton(getString(R.string.discard), (dialog, which) -> finish());
        builder.create().show();

    }

    private void showInfoDialog(Metadata metadata) {

        StringBuilder stringBuilder = new StringBuilder();

        for (Directory directory : metadata.getDirectories()) {

            for (Tag tag : directory.getTags()) {
                if (Arrays.asList(TAGS_NAME).contains(tag.getTagName())) {

                    stringBuilder
                            .append(tag.getTagName())
                            .append("\n").append("|\t\t")
                            .append(tag.getDescription())
                            .append("\n\n");

                }
            }

        }

        ViewGroup viewGroup = findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.layout_img_info, viewGroup, false);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        TextView imgInfo = dialogView.findViewById(R.id.img_info);
        Button share = dialogView.findViewById(R.id.share);
        Button close = dialogView.findViewById(R.id.close);
        Button copy = dialogView.findViewById(R.id.copy);

        imgInfo.setText(stringBuilder.toString());

        builder.setView(dialogView);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        share.setOnClickListener(view -> {

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, stringBuilder.toString());
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        });

        copy.setOnClickListener(view -> {

            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText(getString(R.string.app_name), stringBuilder.toString());

            if (clipboardManager == null) {

                Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
                return;
            }

            clipboardManager.setPrimaryClip(clipData);

            Toast.makeText(this, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();

        });

        close.setOnClickListener(view -> alertDialog.dismiss());

    }

    @Override
    public void onFilterSelected(PhotoFilter photoFilter) {
        mPhotoEditor.setFilterEffect(photoFilter);
    }

    @Override
    public void onToolSelected(ToolType toolType) {
        switch (toolType) {
            case BRUSH:
                mPhotoEditor.setBrushDrawingMode(true);
                mTxtCurrentTool.setText(R.string.label_brush);
                mPropertiesBSFragment.show(getSupportFragmentManager(), mPropertiesBSFragment.getTag());
                break;
            case TEXT:
                TextEditorDialogFragment textEditorDialogFragment = TextEditorDialogFragment.show(this);
                textEditorDialogFragment.setOnTextEditorListener((inputText, colorCode) -> {
                    final TextStyleBuilder styleBuilder = new TextStyleBuilder();
                    styleBuilder.withTextColor(colorCode);

                    mPhotoEditor.addText(inputText, styleBuilder);
                    mTxtCurrentTool.setText(R.string.label_text);
                });
                break;
            case ERASER:
                mPhotoEditor.brushEraser();
                mTxtCurrentTool.setText(R.string.label_eraser);
                break;
            case FILTER:
                mTxtCurrentTool.setText(R.string.label_filter);
                showFilter(true);
                break;
            case EMOJI:
                mEmojiBSFragment.show(getSupportFragmentManager(), mEmojiBSFragment.getTag());
                break;
            case STICKER:
                mStickerBSFragment.show(getSupportFragmentManager(), mStickerBSFragment.getTag());
                break;
        }
    }


    void showFilter(boolean isVisible) {
        mIsFilterVisible = isVisible;
        mConstraintSet.clone(mRootView);

        if (isVisible) {
            mConstraintSet.clear(mRvFilters.getId(), ConstraintSet.START);
            mConstraintSet.connect(mRvFilters.getId(), ConstraintSet.START,
                    ConstraintSet.PARENT_ID, ConstraintSet.START);
            mConstraintSet.connect(mRvFilters.getId(), ConstraintSet.END,
                    ConstraintSet.PARENT_ID, ConstraintSet.END);
        } else {
            mConstraintSet.connect(mRvFilters.getId(), ConstraintSet.START,
                    ConstraintSet.PARENT_ID, ConstraintSet.END);
            mConstraintSet.clear(mRvFilters.getId(), ConstraintSet.END);
        }

        ChangeBounds changeBounds = new ChangeBounds();
        changeBounds.setDuration(350);
        changeBounds.setInterpolator(new AnticipateOvershootInterpolator(1.0f));
        TransitionManager.beginDelayedTransition(mRootView, changeBounds);

        mConstraintSet.applyTo(mRootView);
    }

    @Override
    public void onBackPressed() {
        if (mIsFilterVisible) {
            showFilter(false);
            mTxtCurrentTool.setText(R.string.app_name);
        } else if (!mPhotoEditor.isCacheEmpty()) {
            showSaveDialog();
        } else {
            super.onBackPressed();
        }
    }
}