package me.samlss.inkjet.managers;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import me.samlss.ebs.EBSClient;
import me.samlss.ebs.EBSControlParameter;
import me.samlss.ebs.EBSCreateBluetoothParameter;
import me.samlss.ebs.EBSCreateLineSeparatorParameter;
import me.samlss.ebs.EBSCreateObjectParameter;
import me.samlss.ebs.EBSCreateProjectParameter;
import me.samlss.ebs.EBSCreateTextParameter;
import me.samlss.ebs.EBSDeleteDirParameter;
import me.samlss.ebs.EBSDeleteProjectParameter;
import me.samlss.ebs.EBSOpParameter;
import me.samlss.ebs.EBSOpenProjectParameter;
import me.samlss.ebs.EBSParameter;
import me.samlss.ebs.EBSRequestCallback;
import me.samlss.ebs.EBSSaveProjectParameter;
import me.samlss.ebs.EBSWorkspaceSizeParameter;
import me.samlss.ebs.response.EBSBaseResponse;
import me.samlss.ebs.response.EBSCreateObjResponse;
import me.samlss.ebs.response.EBSProjObjResponse;
import me.samlss.framework.log.PLog;
import me.samlss.framework.utils.ListUtils;
import me.samlss.inkjet.config.InkConfig;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description
 */
public class WifiManager {
    private volatile static WifiManager sInstance;
    private String ip;
    private AtomicBoolean isConnected = new AtomicBoolean(false);

    private WifiManager(){
        ip = InkConfig.getWifiIp();
    }

    public static WifiManager getInstance(){
        if (sInstance == null){
            synchronized (WifiManager.class){
                if (sInstance == null){
                    sInstance = new WifiManager();
                }
            }
        }

        return sInstance;
    }

    public void connect(final ConnectCallback callback){
        EBSClient.getInstance().setIp(InkConfig.getWifiIp());
        EBSClient.getInstance().requestAsync(new EBSOpParameter(EBSOpParameter.Op.CHECK_CONNECTED), new EBSRequestCallback() {
            @Override
            public void onDone(String response) {
                if (!TextUtils.isEmpty(response)){
                    try{
                        JSONObject object = JSON.parseObject(response);
                        String status = object.getString("Status");
                        if (!TextUtils.isEmpty(status) && status.equalsIgnoreCase("OK")){
                            isConnected.set(true);
                            if (callback != null){
                                callback.onConnect(true);
                            }
                        }else{
                            isConnected.set(false);
                            if (callback != null){
                                callback.onConnect(false);
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        isConnected.set(false);
                        if (callback != null){
                            callback.onConnect(false);
                        }
                    }
                }else{
                    isConnected.set(false);
                    if (callback != null){
                        callback.onConnect(false);
                    }
                }
            }
        });
    }

    public boolean isConnected(){
        return isConnected.get();
    }

    /**
     * 删除wifi项目，异步
     * */
    public void deleteWifiProjectAsync(String prjName, boolean isDir){
        if (TextUtils.isEmpty(prjName)){
            return;
        }

        EBSParameter parameter;
        if (isDir){
            parameter = new EBSDeleteDirParameter(prjName);
        }else{
            parameter = new EBSDeleteProjectParameter(prjName);
        }

        EBSClient.getInstance().requestAsync(parameter, new EBSRequestCallback() {
            @Override
            public void onDone(String response) {
                PLog.e("delete project finish: "+response);
            }
        });
    }

    private boolean isResponseOk(EBSBaseResponse response){
        return response != null
                && !(TextUtils.isEmpty(response.getStatus()))
                && response.getStatus().equalsIgnoreCase("OK");
    }

    private boolean isResponseOk(EBSCreateObjResponse response){
        return response != null
                && !(TextUtils.isEmpty(response.getStatus()))
                && response.getStatus().equalsIgnoreCase("OK");
    }

    private boolean createBluetoothReal(int x, int y, int objectId, String objectName, List<String> objNameList){
        EBSCreateBluetoothParameter bluetoothParameter = new EBSCreateBluetoothParameter();
        bluetoothParameter.setObjectID(objectId)
                .setObjectName(objectName)
                .setFontName(InkConfig.getFontParam())
                .setFontSize(InkConfig.getFontSize())
                .setX(x)
                .setY(y);

//        if (!ListUtils.isEmpty(objNameList)){
//            bluetoothParameter.setSeparator(objNameList.size() + 1);
//            for (int i = 0; i < objNameList.size(); i++){
//                String sObjectName = objNameList.get(i);
//                switch (i){
//                    case 0:
//                        bluetoothParameter.setSeparatorObject1(sObjectName);
//                        break;
//
//                    case 1:
//                        bluetoothParameter.setSeparatorObject2(sObjectName);
//                        break;
//
//                    case 2:
//                        bluetoothParameter.setSeparatorObject3(sObjectName);
//                        break;
//
//                    case 3:
//                        bluetoothParameter.setSeparatorObject4(sObjectName);
//                        break;
//
//                    case 4:
//                        bluetoothParameter.setSeparatorObject5(sObjectName);
//                        break;
//
//                    case 5:
//                        bluetoothParameter.setSeparatorObject6(sObjectName);
//                        break;
//
//                    case 6:
//                        bluetoothParameter.setSeparatorObject7(sObjectName);
//                        break;
//
//                    case 7:
//                        bluetoothParameter.setSeparatorObject8(sObjectName);
//                        break;
//
//                    case 8:
//                        bluetoothParameter.setSeparatorObject9(sObjectName);
//                        break;
//                }
//            }
//        }

        String response = EBSClient.getInstance().request(bluetoothParameter);
        PLog.d("createBluetoothReal1 =? response = "+response);
        EBSProjObjResponse createBluetoothRes =  JSON.parseObject(response, EBSProjObjResponse.class);
        if (!isResponseOk(createBluetoothRes) || createBluetoothRes.getGroup() == null){
            return false;
        }

        bluetoothParameter
                .setW(createBluetoothRes.getGroup().getW())
                .setH(createBluetoothRes.getGroup().getH());

        response = EBSClient.getInstance().request(bluetoothParameter);
        PLog.d("createBluetoothReal2 =? response = "+response);
        createBluetoothRes =  JSON.parseObject(response, EBSProjObjResponse.class);
        if (!isResponseOk(createBluetoothRes) || createBluetoothRes.getGroup() == null){
            return false;
        }

        return true;
    }

    private boolean createBluetoothObj(int x, int y, int objectId, String objectName, List<String> objNameList){
        EBSCreateObjectParameter creBtObjParameter = new EBSCreateObjectParameter();
        creBtObjParameter.setX(x).setY(y).setType(30);
        String response = EBSClient.getInstance().request(creBtObjParameter);
        EBSCreateObjResponse createBtObjRes =  JSON.parseObject(response, EBSCreateObjResponse.class);
        if (!isResponseOk(createBtObjRes)){
            return false;
        }

        return createBluetoothReal(x, y, objectId, URLEncoder.encode(objectName), objNameList);
    }

    private TextResult createTextReal(int x, int y, int objectId, String objectName, String objectText){
        TextResult textResult = new TextResult();

        EBSCreateTextParameter createTextParameter = new EBSCreateTextParameter();
        createTextParameter.setObjectID(objectId)
                .setObjectName(objectName)
                .setFontName(InkConfig.getFontParam())
                .setObjectText(objectText)
                .setFontSize(InkConfig.getFontSize())
                .setX(x)
                .setY(y);
        String response = EBSClient.getInstance().request(createTextParameter);
        PLog.e("createTextReal1 =? response = "+response);
        EBSProjObjResponse createTextRes =  JSON.parseObject(response, EBSProjObjResponse.class);
        if (!isResponseOk(createTextRes) || createTextRes.getGroup() == null){
            return textResult;
        }

        createTextParameter
                .setW(createTextRes.getGroup().getW())
                .setH(createTextRes.getGroup().getH());

        response = EBSClient.getInstance().request(createTextParameter);
        PLog.e("createTextReal2 =? response = "+response);
        createTextRes =  JSON.parseObject(response, EBSProjObjResponse.class);

        if (!isResponseOk(createTextRes) || createTextRes.getGroup() == null){
            return textResult;
        }

        textResult.w = createTextParameter.getW();
        textResult.h = createTextParameter.getH();
        textResult.result = true;
        return textResult;
    }

    private TextResult createTextObj(int x, int y, int objectId, String objectName, String objectText){
        EBSCreateObjectParameter createTextParameter = new EBSCreateObjectParameter();
        createTextParameter.setX(x).setY(y).setType(0);
        String response = EBSClient.getInstance().request(createTextParameter);
        EBSCreateObjResponse createTxtObjRes =  JSON.parseObject(response, EBSCreateObjResponse.class);

        if (!isResponseOk(createTxtObjRes)){
            return new TextResult();
        }

        return createTextReal(x, y, objectId, objectName, objectText);
    }

    private boolean createLineSeparator(int x, int y){
        EBSCreateLineSeparatorParameter lineSeparatorParameter = new EBSCreateLineSeparatorParameter();
        lineSeparatorParameter.setX(x).setY(y);
        String response = EBSClient.getInstance().request(lineSeparatorParameter);
        EBSBaseResponse createLSObjRes =  JSON.parseObject(response, EBSBaseResponse.class);

        return isResponseOk(createLSObjRes);
    }

    /**
     * 创建wifi项目，耗时操作
     *
     * @param contents 行列表
     * @return 是否创建成功
     * */
    public boolean createWifiProject(String prjName, boolean createProjectDir, List<String> contents){
        if (ListUtils.isEmpty(contents)){
            return false;
        }

        try {
            //这里处理一下空的content
            List<String> emptyContents = new ArrayList<>();
            for (String content : contents){
                if (TextUtils.isEmpty(content)){
                    emptyContents.add(content);
                }
            }

            contents.removeAll(emptyContents);

            String response;
            int x = 0;
            int y = 0;
            int objectId = 0;
            String objectName = null;
            List<String> objNameList = new ArrayList<>();
            String projectPath = (createProjectDir ? ("/" + prjName + "/" + prjName) : ("/" + prjName)) + ".prj";

            EBSCreateProjectParameter parameter = new EBSCreateProjectParameter();
            parameter.setCreateProjectDir(createProjectDir);
            parameter.setFileName(prjName);
            response = EBSClient.getInstance().request(parameter);
            EBSBaseResponse createProjRes = JSON.parseObject(response, EBSBaseResponse.class);
            if (!isResponseOk(createProjRes)){
                return false;
            }

            response = EBSClient.getInstance().request(new EBSWorkspaceSizeParameter(5000));
            EBSBaseResponse wpsRes = JSON.parseObject(response, EBSBaseResponse.class);
            if (!isResponseOk(wpsRes)){
                return false;
            }

            if (contents.size() == 1) {
                objectName = contents.get(0);
            } else {
                String objTextName;
                for (int i = 0; i < contents.size() - 1; i++){
                    TextResult result = createTextObj(x, y, objectId, objTextName =("Text" + i), URLEncoder.encode(contents.get(i)));
                    if (result == null || !result.result){
                        return false;
                    }

                    x += result.w;
                    if (!createLineSeparator(x, y)){
                        return false;
                    }

                    x += 5;
                    objectId+=2;
                    objNameList.add(objTextName);
                }

                objectName = contents.get(contents.size() - 1);
            }

            if (!createBluetoothObj(x, y, objectId, objectName, objNameList)){
                return false;
            }

            EBSSaveProjectParameter saveProjectParameter = new EBSSaveProjectParameter();
            saveProjectParameter.setFilename(projectPath);
            response = EBSClient.getInstance().request(saveProjectParameter);
            EBSBaseResponse saveProjRes = JSON.parseObject(response, EBSBaseResponse.class);
            if (!isResponseOk(saveProjRes)){
                return false;
            }

            EBSOpenProjectParameter openProjectParameter = new EBSOpenProjectParameter();
            openProjectParameter.setProjectPath(projectPath);
            response = EBSClient.getInstance().request(openProjectParameter);
            EBSBaseResponse openProjectRes = JSON.parseObject(response, EBSBaseResponse.class);
            if (!isResponseOk(openProjectRes)){
                return false;
            }

            response = EBSClient.getInstance().request(new EBSControlParameter()
                    .setDirection(InkConfig.getDirection())
                    .setDistance(InkConfig.getDistance())
                    .setInterval(InkConfig.getInterval())
                    .setHorizontalFlip(InkConfig.getHorizontalFlip())
                    .setVerticalFlip(InkConfig.getVerticalFlip())
                    .setSpotSize(InkConfig.getSpotSize())
                    .setPressure(InkConfig.getPressure())
                    .setRepeat(InkConfig.getRepeat())
                    .setResolution(InkConfig.getResolution()));
            EBSBaseResponse setParamRes = JSON.parseObject(response, EBSBaseResponse.class);

            if (!isResponseOk(setParamRes)){
                return false;
            }

            EBSOpParameter printParameter = new EBSOpParameter(EBSOpParameter.Op.PRINT);
            response = EBSClient.getInstance().request(printParameter);
            EBSBaseResponse printRes = JSON.parseObject(response, EBSBaseResponse.class);
            if (!isResponseOk(printRes)){
                return false;
            }

            return true;
        }catch (Exception e){
            e.printStackTrace();
        }

        return false;
    }

    public void destroy(){
        isConnected.set(false);
    }

    private class TextResult{
        int w;
        int h;
        boolean result;
    }

    public interface ConnectCallback{
        void onConnect(boolean isConnected);
    }
}
