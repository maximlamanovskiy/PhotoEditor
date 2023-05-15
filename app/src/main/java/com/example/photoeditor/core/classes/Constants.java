package com.example.photoeditor.core.classes;

import android.os.Environment;
import com.example.photoeditor.R;

final public class Constants {

    public final static String EMPTY = "";
    public final static Integer REQUEST_CODE = 4320;
    public final static String TEMP_PICTURE_DIRECTORY = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/.spe-temp";
    public final static String FINAL_PICTURE_DIRECTORY = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Simple Photo Editor";

    public final static Integer[] COLORS = {
            R.color.red,
            R.color.orange,
            R.color.goldenYellow,
            R.color.limegreen,
            R.color.blue,
            R.color.indigo,
            R.color.violet
    };

    public final static String[] TAGS_NAME = new String[]{"Compression Type", "Data Precision", "Image Height", "Image Width", "Image Description", "Make",
            "Model", "Orientation", "X Resolution", "Y Resolution", "Resolution Unit", "Software", "Date/Time",
            "Exposure Time", "F-Number", "Exposure Program", "ISO Speed Ratings", "Exif Version", "Date/Time Original",
            "Date/Time Digitized", "Components Configuration", "Exposure Bias Value", "Metering Mode", "White Balance",
            "Flash", "Focal Length", "Sub-Sec Time Original", "Sub-Sec Time Digitized", "Color Space", "Exif Image Width",
            "Exif Image Height", "Exposure Mode", "White Balance Mode", "Digital Zoom Ratio", "Scene Capture Type", "Detected File Type Name",
            "Detected File Type Long Name", "Detected MIME Type", "Expected File Name Extension", "File Name", "File Size", "File Modified Date"
    };
}