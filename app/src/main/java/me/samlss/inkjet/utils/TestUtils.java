package me.samlss.inkjet.utils;

import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import me.samlss.framework.utils.AppUtils;
import me.samlss.framework.utils.FileUtils;
import me.samlss.framework.utils.PathUtils;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description 测试工具类
 */
public class TestUtils {
    private TestUtils(){}

    public static void copyDb(){
        File dbFile = new File(PathUtils.getInternalAppDbsPath(), "ink");
        if (dbFile.exists()){
            return;
        }

        try {
            AssetManager am = AppUtils.getApp().getAssets();
            InputStream is = am.open("ink");
            FileUtils.createDirIfNotExists(PathUtils.getInternalAppDbsPath());

            FileOutputStream fos = new FileOutputStream(dbFile);
            byte[] buffer=new byte[1024];
            int count = 0;
            while((count = is.read(buffer))>0){
                fos.write(buffer,0,count);
            }
            fos.flush();
            fos.close();
            is.close();
            Log.e("TAG","copy db finish...");
        }catch (Exception e){
            e.printStackTrace();
            Log.e("TAG","copy db error: " + e.getMessage());
        }
    }
}
