package com.aiworks.hdrdemo;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

import com.aiworks.android.ui.CompView;
import com.mediatek.camera.R;

public class CompareActivity extends AppCompatActivity {

    private static final String TAG = "CompareActivity";

    private CompView mCompareView;

    public static Bitmap mOriginImage;

    public static Bitmap mResultImage;

    private int mImageWidth;

    private int mImageHeight;

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare);

        if (mOriginImage == null || mResultImage == null) {
            Toast.makeText(this, "", Toast.LENGTH_LONG).show();
            return;
        } else {
            Toast.makeText(this, "The picture has been saved in /sdcard/DCIM/hdr.", Toast.LENGTH_LONG).show();
        }

        mImageWidth = mOriginImage.getWidth();
        mImageHeight = mOriginImage.getHeight();

        mCompareView = findViewById(R.id.compare_view);
        mCompareView.setCallback(new CompView.Callback() {
            @Override
            public void onReady() {
                mCompareView.compare(mResultImage, mOriginImage, mImageWidth, mImageHeight);
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mOriginImage.recycle();
        mResultImage.recycle();
    }

}
