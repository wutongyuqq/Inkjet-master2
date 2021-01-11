package me.samlss.inkjet.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.text.TextUtils;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import me.samlss.framework.log.PLog;
import me.samlss.framework.utils.CacheMemoryUtils;
import me.samlss.framework.utils.EncodeUtils;
import me.samlss.framework.utils.FileUtils;
import me.samlss.framework.utils.IoUtils;
import me.samlss.framework.utils.PathUtils;
import me.samlss.framework.utils.ScreenUtils;
import me.samlss.framework.utils.ThreadUtils;
import me.samlss.inkjet.config.InkConfig;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description 图片处理工具
 */
public class ImageUtils {
    public static String IMAGE_CACHE_DIR = PathUtils.getInternalAppCachePath() + File.separator + "ink-images";

    private ImageUtils(){}

    public static ImageTask showImage(String url, ImageView imageView){
        if (TextUtils.isEmpty(url) || imageView == null){
            return null;
        }

        Bitmap bitmap = CacheMemoryUtils.getInstance().get(url);
        if (bitmap != null && !bitmap.isRecycled()){
            imageView.setImageBitmap(bitmap);
            return null;
        }

        ImageTask imageTask = new ImageTask(url, imageView);
        ThreadUtils.executeByIo(imageTask);
        return imageTask;
    }

    public static void deleteCacheImageDir(){
        FileUtils.deleteDir(IMAGE_CACHE_DIR);
    }

    public static void deleteCacheMemory(){
        Bitmap logoBitmap = CacheMemoryUtils.getInstance().get(InkConfig.getCacheCompanyLogoUrl());
        Bitmap bannerBitmap = CacheMemoryUtils.getInstance().get(InkConfig.getCacheCompanyBannerUrl());

        CacheMemoryUtils.getInstance().remove(InkConfig.getCacheCompanyLogoUrl());
        CacheMemoryUtils.getInstance().remove(InkConfig.getCacheCompanyBannerUrl());

        recycleBitmap(logoBitmap);
        recycleBitmap(bannerBitmap);
    }

    public static void recycleBitmap(Bitmap bitmap){
        try{
            if (bitmap == null){
                return;
            }

            if (!bitmap.isRecycled()){
                bitmap.recycle();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static class ImageTask extends ThreadUtils.Task<Bitmap> {
        private String mUrl;
        private WeakReference<ImageView> mImageViewRef;

        public ImageTask(String url, ImageView imageView){
            mUrl = url;
            mImageViewRef = new WeakReference<>(imageView);
        }

        @Nullable
        @Override
        public Bitmap doInBackground() throws Throwable {
            String baseUrl = new String(EncodeUtils.base64Encode(mUrl));
            File pictureFile = new File(IMAGE_CACHE_DIR, baseUrl);
            if (pictureFile.exists()){
                return loadLocalPictureFile(pictureFile.getPath());
            }

            URL url = new URL(mUrl);
            //获取连接
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            //使用GET方法访问网络
            connection.setRequestMethod("GET");
            //超时时间为20秒
            connection.setConnectTimeout(20 * 1000);
            //获取返回码
            int code = connection.getResponseCode();
            if (code == 200) {
                FileUtils.createDirIfNotExists(IMAGE_CACHE_DIR);

                InputStream inputStream = connection.getInputStream();
                IoUtils.writeFileFromIS(pictureFile, inputStream);

                return loadLocalPictureFile(pictureFile.getPath());
            }

            return null;
        }

        private Bitmap loadLocalPictureFile(String path){
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;  //只去读图片的头信息,不去解析真实的位图，即不加载bitmap，避免OOM
            Bitmap bitmap = BitmapFactory.decodeFile(path, opts);// 此时返回bm为空

            if (opts.outWidth > ScreenUtils.getScreenWidth()) {
                opts.inSampleSize = Math.round((float) opts.outWidth / (float) ScreenUtils.getScreenWidth());
            }

            opts.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(path, opts);
        }

        /**
         * 得到bitmap的大小
         */
        public static int getBitmapSize(Bitmap bitmap) {
            if (bitmap == null) return 0;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {    //API 19
                return bitmap.getAllocationByteCount();
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {//API 12
                return bitmap.getByteCount();
            }
            // 在低版本中用一行的字节x高度
            return bitmap.getRowBytes() * bitmap.getHeight();                //earlier version
        }

        @Override
        public void onSuccess(@Nullable Bitmap result) {
            PLog.e("bitmap size: "+getBitmapSize(result));
            CacheMemoryUtils.getInstance().put(mUrl, result);
            if (mImageViewRef != null && mImageViewRef.get() != null){
                PLog.e("set image bitmap: "+mImageViewRef.get().getContext());
                mImageViewRef.get().setImageBitmap(result);
                mImageViewRef.clear();
            }
        }

        @Override
        public void onCancel() {
            if (mImageViewRef != null){
                mImageViewRef.clear();
            }
        }

        @Override
        public void onFail(Throwable t) {
            if (mImageViewRef != null){
                mImageViewRef.clear();
            }
        }
    }
}
