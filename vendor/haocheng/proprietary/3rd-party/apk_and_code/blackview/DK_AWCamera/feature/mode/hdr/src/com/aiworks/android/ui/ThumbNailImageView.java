package com.aiworks.android.ui;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import androidx.annotation.IntDef;
import javax.annotation.Nonnull;
import android.util.AttributeSet;
import android.util.Log;

import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.annotation.Nullable;

public class ThumbNailImageView extends androidx.appcompat.widget.AppCompatImageView {
    private static final String TAG = "ThumbNailImageView";

    public static final int INCLUDE_IMAGE = 1;
    public static final int INCLUDE_VIDEO = 2;
    public static final int INCLUDE_ALL = INCLUDE_IMAGE | INCLUDE_VIDEO;

    private @ThumbNailType int mMediaType;
    private Uri mUri;
    private ThumbNailTask mTask;
    private String mThumbnailDirectory = Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera/";

    private boolean mIsRound = true;
    private Path mRoundPath;
    private int mRoundRadius = 20;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({INCLUDE_IMAGE, INCLUDE_VIDEO, INCLUDE_ALL})
    public @interface ThumbNailType {
    }

    public ThumbNailImageView(Context context) {
        super(context);
    }

    public ThumbNailImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public ThumbNailImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setThumbnailType(@ThumbNailType int type) {
        mMediaType = type;
    }

    public void setThumbnailDirectory(String directory) {
        mThumbnailDirectory = directory;
    }

    @Override
    public void setImageURI(@Nullable Uri uri) {
        super.setImageURI(uri);
    }

    public Uri getThumbnailUri() {
        return mUri;
    }

    private void setThumbnail(Uri uri, Bitmap bitmap) {
        mUri = uri;
        setBackground(null);
        setImageBitmap(bitmap);
        mTask = null;
    }

    public void updateThumbnailUri(Uri uri) {
        if (uri == null) {
            cancelTask();
            mUri = null;
            return;
        }

        if (!uri.equals(mUri)) {
            cancelTask();
            mTask = new ThumbNailTask(this, uri);
            mTask.execute();
        }
    }

    private void cancelTask() {
        if (mTask != null) {
            mTask.cancel(true);
            mTask = null;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus) {
            if (mUri == null) {
                mTask = new ThumbNailTask(this);
                mTask.execute(mThumbnailDirectory);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!mIsRound) {
            super.onDraw(canvas);
            return;
        }

        if (mRoundPath == null) {
            mRoundPath = new Path();
            mRoundPath.addRoundRect(new RectF(0, 0, getWidth(), getHeight()), mRoundRadius, mRoundRadius, Path.Direction.CW);
        }
        canvas.clipPath(mRoundPath);
        super.onDraw(canvas);
    }

    public static class ThumbNailTask extends AsyncTask<String, Void, Bitmap> {

        private WeakReference<ThumbNailImageView> ref;
        private int defaultResId = -1;
        private Uri baseUri;
        private String selection;
        private String order;
        private ContentResolver resolver;
        private Uri itemUri;

        public ThumbNailTask(ThumbNailImageView view) {
            this(view, -1);
        }

        public ThumbNailTask(ThumbNailImageView view, int defaultResId) {
            this(view, null, defaultResId);
        }

        public ThumbNailTask(ThumbNailImageView view, Uri uri) {
            this(view, uri, -1);
        }

        public ThumbNailTask(ThumbNailImageView view, Uri uri, int defaultResId) {
            ref = new WeakReference<>(view);
            resolver = view.getContext().getContentResolver();
            itemUri = uri;
            this.defaultResId = defaultResId;
            init(view.mMediaType);
        }

        private void init(@ThumbNailType int type) {
            switch (type) {
                case INCLUDE_IMAGE:
                    baseUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    selection = MediaStore.Images.ImageColumns.BUCKET_ID + "=?";
                    order = MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC, "
                            + MediaStore.Images.ImageColumns._ID + " ASC";
                    break;
                case INCLUDE_VIDEO:
                    baseUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    selection = MediaStore.Video.VideoColumns.BUCKET_ID + "=?";
                    order = MediaStore.Video.VideoColumns.DATE_TAKEN + " DESC, "
                            + MediaStore.Video.VideoColumns._ID + " DESC";
                    break;
                default:
                    baseUri = MediaStore.Files.getContentUri("external");
                    selection = MediaStore.Video.VideoColumns.BUCKET_ID + "=? AND ("
                            + MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                            + MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                            + ")";
                    order = MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC, "
                            + MediaStore.Images.ImageColumns._ID + " ASC";
            }
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            if (itemUri == null) {
                itemUri = getLatestUri(strings != null && strings.length > 0 ? strings[0] : null);
            }

            if (itemUri == null) return null;
            ThumbNailImageView imageView = ref.get();
            if (imageView == null) {
                Log.w(TAG, "Thumbnail view not exist");
                return null;
            }

            int width = imageView.getWidth();
            int height = imageView.getHeight();
            if (width <= 0 || height <= 0) {
                Log.w(TAG, "Thumbnail view width or height: " + width + ", " + height);
                return null;
            }
            int targetSize = Math.min(width, height);
            Bitmap bmp = getUriThumbnail(itemUri, targetSize);
            return bmp;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            ThumbNailImageView thumbnailView = ref.get();
            if (thumbnailView == null || isCancelled()) return;

            if (result == null) {
                if (defaultResId != -1) {
                    thumbnailView.setImageResource(defaultResId);
                }
                return;
            }

            thumbnailView.setThumbnail(itemUri, result);
        }

        private Uri getLatestUri(String dir) {
            if (dir == null) return null;

            File file = new File(dir);
            if (!file.exists() || !file.isDirectory()) return null;

            while (dir.endsWith(File.separator)) {
                dir = dir.substring(0, dir.length() - 1);
            }
            int bucketId = dir.toLowerCase().hashCode();
            String[] projects = new String[]{MediaStore.Files.FileColumns._ID};
            String[] selectionArgs = new String[]{String.valueOf(bucketId)};
            Uri uri = baseUri.buildUpon().appendQueryParameter("limit", "0,1").build();

            Cursor cursor = null;
            try {
                cursor = resolver.query(uri, projects, selection, selectionArgs, order);
                if (cursor != null && cursor.moveToNext()) {
                    int id = cursor.getInt(0);

                    return baseUri.buildUpon().appendPath(String.valueOf(id)).build();
                }
            } finally {
                closeSilently(cursor);
            }

            return null;
        }

        private Bitmap getUriThumbnail(Uri uri, int targetSize) {
            if (uri == null || targetSize < 1) return null;

            String type = resolver.getType(uri);
            boolean isImage = type != null && type.contains("image");
            boolean isVideo = type != null && type.contains("video");

            ParcelFileDescriptor fd = null;
            try {
                fd = resolver.openFileDescriptor(uri, "r");
                if (fd == null) return null;

                if (isImage) {
                    return decodeImage(fd.getFileDescriptor(), targetSize);
                }

                if (isVideo)
                    return decodeVideo(fd.getFileDescriptor(), targetSize);
            } catch (FileNotFoundException fnfe) {
                Log.e(TAG, "openFileDescriptor exception: " + uri);
            } finally {
                closeSilently(fd);
            }

            return null;
        }

        private Bitmap decodeImage(FileDescriptor fd, int targetSize) {
            Options options = new Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFileDescriptor(fd, null, options);
            int w = options.outWidth;
            int h = options.outHeight;
            if (w == -1 || h == -1) {
                Log.e(TAG, "decodeImage fail: " + w + ", " + h);
                return null;
            }

            // We center-crop the original image as it's micro thumbnail. In this case,
            // we want to make sure the shorter side >= "targetSize".
            float scale = (float) targetSize / Math.min(w, h);
            options.inSampleSize = computeSampleSizeLarger(scale);

            options.inJustDecodeBounds = false;
            options.inMutable = true;
            Bitmap result = BitmapFactory.decodeFileDescriptor(fd, null, options);
            if (result == null) return null;

            scale = (float) targetSize / Math.min(result.getWidth(), result.getHeight());
            if (scale <= 0.5) {
                result = resizeBitmapByScale(result, scale, true);
            }

            return result;
        }

        private Bitmap decodeVideo(FileDescriptor fd, int targetSize) {
            Bitmap origBmp = createVideoThumbnail(fd);
            if (origBmp == null || origBmp.isRecycled()) {
                Log.e(TAG, "createVideoThumbnail origBmp: " + origBmp);
                return null;
            }

            Bitmap result = resizeAndCropCenter(origBmp, targetSize, true);
            return result;
        }

        public static Bitmap createVideoThumbnail(FileDescriptor fd) {
            // MediaMetadataRetriever is available on API Level 8
            // but is hidden until API Level 10
            Class<?> clazz = null;
            Object instance = null;
            try {
                clazz = Class.forName("android.media.MediaMetadataRetriever");
                instance = clazz.newInstance();

                Method method = clazz.getMethod("setDataSource", FileDescriptor.class);
                method.invoke(instance, fd);

                // The method name changes between API Level 9 and 10.
                if (Build.VERSION.SDK_INT <= 9) {
                    return (Bitmap) clazz.getMethod("captureFrame").invoke(instance);
                } else {
                    byte[] data = (byte[]) clazz.getMethod("getEmbeddedPicture").invoke(instance);
                    if (data != null) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        if (bitmap != null) return bitmap;
                    }
                    return (Bitmap) clazz.getMethod("getFrameAtTime").invoke(instance);
                }
            } catch (IllegalArgumentException ex) {
                // Assume this is a corrupt video file
            } catch (RuntimeException ex) {
                // Assume this is a corrupt video file.
            } catch (InstantiationException e) {
                Log.e(TAG, "createVideoThumbnail", e);
            } catch (InvocationTargetException e) {
                Log.e(TAG, "createVideoThumbnail", e);
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "createVideoThumbnail", e);
            } catch (NoSuchMethodException e) {
                Log.e(TAG, "createVideoThumbnail", e);
            } catch (IllegalAccessException e) {
                Log.e(TAG, "createVideoThumbnail", e);
            } finally {
                try {
                    if (instance != null) {
                        clazz.getMethod("release").invoke(instance);
                    }
                } catch (Exception ignored) {
                }
            }
            return null;
        }

        public static Bitmap resizeAndCropCenter(Bitmap bitmap, int size, boolean recycle) {
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            if (w == size && h == size) return bitmap;

            // scale the image so that the shorter side equals to the target;
            // the longer side will be center-cropped.
            float scale = (float) size / Math.min(w,  h);

            Bitmap target = Bitmap.createBitmap(size, size, getConfig(bitmap));
            int width = Math.round(scale * bitmap.getWidth());
            int height = Math.round(scale * bitmap.getHeight());
            Canvas canvas = new Canvas(target);
            canvas.translate((size - width) / 2f, (size - height) / 2f);
            canvas.scale(scale, scale);
            Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
            canvas.drawBitmap(bitmap, 0, 0, paint);
            if (recycle) bitmap.recycle();
            return target;
        }

        // Find the min x that 1 / x >= scale
        public static int computeSampleSizeLarger(float scale) {
            int initialSize = (int) Math.floor(1d / scale);
            if (initialSize <= 1) return 1;

            return initialSize <= 8
                    ? prevPowerOf2(initialSize)
                    : initialSize / 8 * 8;
        }

        // Returns the previous power of two.
        // Returns the input if it is already power of 2.
        // Throws IllegalArgumentException if the input is <= 0
        public static int prevPowerOf2(int n) {
            if (n <= 0) throw new IllegalArgumentException();
            return Integer.highestOneBit(n);
        }

        public static Bitmap resizeBitmapByScale(
                Bitmap bitmap, float scale, boolean recycle) {
            int width = Math.round(bitmap.getWidth() * scale);
            int height = Math.round(bitmap.getHeight() * scale);
            if (width == bitmap.getWidth()
                    && height == bitmap.getHeight()) return bitmap;
            Bitmap target = Bitmap.createBitmap(width, height, getConfig(bitmap));
            Canvas canvas = new Canvas(target);
            canvas.scale(scale, scale);
            Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
            canvas.drawBitmap(bitmap, 0, 0, paint);
            if (recycle) bitmap.recycle();
            return target;
        }

        private static Bitmap.Config getConfig(Bitmap bitmap) {
            Bitmap.Config config = bitmap.getConfig();
            if (config == null) {
                config = Bitmap.Config.ARGB_8888;
            }
            return config;
        }

        public static void closeSilently(Closeable closeable) {
            if (closeable == null) return;

            try {
                closeable.close();
            } catch (Throwable throwable) {
            }
        }
    }
}
