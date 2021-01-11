package me.samlss.inkjet.utils;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description
 */
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description utils of sharing of system
 */
public class SystemShareUtils {
    private SystemShareUtils(){
        throw new UnsupportedOperationException("Can not be instantiated.");
    }

    /**
     * To share a text by call system share function.
     *
     * @param context The context
     * @param content the content you want to share
     * @param title the title string for share dialog
     * */
    public static void shareText(Context context, String title, String content){
        shareText(context, title, content, null);
    }

    /**
     * To share a text by call system share function.
     *
     * @param context The context
     * @param content the content you want to share
     * @param title the title string for share dialog
     * @param packageName specify which app to share to
     * */
    public static void shareText(Context context, String title, String content, String packageName){
        Intent intent = createDefaultIntent();
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, content);
        if (!TextUtils.isEmpty(packageName)){
            intent.setPackage(packageName);
        }
        context.startActivity(Intent.createChooser(intent, title));
    }

    /**
     * Share file by calling system share function.
     *
     * @param context the context
     * @param title the title string for share dialog
     * @param mimeType the mime type of the file
     * @param uri the uri of the file
     * */
    public static void shareFile(Context context, String title, String mimeType, Uri uri){
        shareFile(context, title, mimeType, uri, null);
    }

    /**
     * Share file by calling system share function.
     *
     * @param context the context
     * @param title the title string for share dialog
     * @param mimeType the mime type of the file
     * @param uri the uri of the file
     * @param packageName specify which app to share to
     * */
    public static void shareFile(Context context, String title, String mimeType, Uri uri, String packageName){
        Intent intent = createDefaultIntent();
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        if (!TextUtils.isEmpty(packageName)){
            intent.setPackage(packageName);
        }
        context.startActivity(Intent.createChooser(intent, title));
    }

    /**
     * Share files by calling system share function.
     *
     * @param context the context
     * @param title the title string for share dialog
     * @param uris the uri of the files
     * */
    public static void shareFiles(Context context, String title, String mimeType, ArrayList<Uri> uris){
        shareFiles(context, title, mimeType, uris, null);
    }

    /**
     * Share files by calling system share function.
     *
     * @param context the context
     * @param title the title string for share dialog
     * @param uris the uri of the files
     * @param packageName specify which app to share to
     * */
    public static void shareFiles(Context context, String title, String mimeType, ArrayList<Uri> uris, String packageName){
        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);

        if (!TextUtils.isEmpty(packageName)){
            intent.setPackage(packageName);
        }

        intent.setType(mimeType);
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        context.startActivity(Intent.createChooser(intent, title));
    }

    /**
     * Create a default intent which is using {@link Intent#ACTION_SEND}
     * */
    private static Intent createDefaultIntent(){
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }
}