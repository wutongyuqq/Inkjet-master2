package me.samlss.inkjet.utils;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.List;

import me.samlss.framework.log.PLog;
import me.samlss.framework.utils.ListUtils;
import me.samlss.framework.utils.ThreadUtils;
import me.samlss.inkjet.db.DbManager;
import me.samlss.inkjet.db.InkAnnal;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description 喷码工具
 */
public class InkjetUtils
{
    private InkjetUtils(){}

    public static void print(final String content){
        if (TextUtils.isEmpty(content)){
            return;
        }

        List<InkAnnal> annals = new ArrayList<>();
        annals.add(createAnnal(content));
        addAnnals(annals);
    }

    public static void print(final List<String> contents){
        PLog.e("print contents: "+ JSON.toJSONString(contents));
        if (ListUtils.isEmpty(contents)){
            return;
        }

        List<InkAnnal> annals = new ArrayList<>();
        for (String content : contents){
            if (TextUtils.isEmpty(content)) continue;
            annals.add(createAnnal(content));
        }
        addAnnals(annals);
    }

    private static InkAnnal createAnnal(String content){
        InkAnnal inkAnnal = new InkAnnal();
        inkAnnal.setContent(content);
        inkAnnal.setPrint_time(System.currentTimeMillis());
        inkAnnal.setLatitude(22.5413810613d);
        inkAnnal.setLongitude(113.9328915627d);
        inkAnnal.setAddress("广东省深圳市南山区粤海街道比克科技大厦");
//        inkAnnal.setUser_id(UserManager.getInstance().getCompanyUserId());
        return inkAnnal;
    }

    public static void addAnnals(List<InkAnnal> inkAnnalList){
        ThreadUtils.executeByIo(new ThreadUtils.Task<Integer>() {
            @Nullable
            @Override
            public Integer doInBackground() throws Throwable {
                DbManager.get().insertInkAnnals(inkAnnalList);
                return inkAnnalList.size();
            }

            @Override
            public void onSuccess(@Nullable Integer result) {
                PLog.e("on annal inserts success, size: "+result);
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onFail(Throwable t) {
                PLog.e("on annal inserts failed: "+t.getMessage());
            }
        });
    }

    public static void checkLogin(){
//        if (TextUtils.isEmpty(UserManager.getInstance().getCompanyUserId())){
//            ActivityUtils.finishAllActivities();
//        }
    }

    public static String getInkAnnalContent(InkAnnal inkAnnal){
        if (inkAnnal == null){
            return "";
        }

        try{
            if (!TextUtils.isEmpty(inkAnnal.getContent())
                    && !inkAnnal.getContent().contains("[")){
                return inkAnnal.getContent();
            }

            String content = "";
            List<String> contents = JSON.parseArray(inkAnnal.getContent(), String.class);
            for (int i = 0; i < contents.size(); i++){
                if (TextUtils.isEmpty(contents.get(i))){
                    continue;
                }

                content += contents.get(i);
                if (i < (contents.size() - 1)){
                    content += " : ";
                }
            }

            return content;
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }
}
