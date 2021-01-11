package me.samlss.inkjet.config;

import com.tencent.mmkv.MMKV;

import java.util.Set;

import me.samlss.framework.log.PLog;
import me.samlss.inkjet.constant.InkConstant;
import me.samlss.inkjet.ui.adapters.FontSectionEntity;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description 配置类
 */
public class InkConfig {
    public static String KEY_USER_COMPANY_CODE = "kucc";
    public static String KEY_USER_COMPANY_EXPIRED = "kusce";
    public static String KEY_COMPANY_QRCODE_URL = "kcpqrcu";
    public static String KEY_COMPANY_HOST = "kch";
    public static String KEY_COMPANY_NAME = "kcn";
    public static String KEY_COMPANY_LOGO_URL = "kclu";
    public static String KEY_COMPANY_BANNER_URL = "kcbu";

    public static String KEY_EXCEL_DATA_PATH = "kedp";
    public static String KEY_EXCEL_SHEET_INDEX = "kesi";

    public static String KEY_IS_AUTO_LOGIN = "kial";
    public static String KEY_NO_MORE_TIPS_FOR_SELECT_BT = "knmtfsb";
    public static String KEY_PRINT_FORMAT = "kpf";
    public static String KEY_END_CHARACTER = "kec";
    public static String KEY_SPLIT_CHARACTER = "ksc";

    public static String KEY_PRINT_START_RETURN_VALUE = "kpsrrv";
    public static String KEY_PRINT_FINISH_RETURN_VALUE = "kpfrv";
    public static String KEY_PRINT_STOP_RETURN_VALUE = "kpsprv";
    public static String KEY_NO_MORE_REQUEST_PERMISSION_TIP_FOR_LOCATION = "knmrp";

    public static String KEY_IS_UPLOAD_PRINT_RECORD = "kiupr";
    public static String KEY_SUPPORT_DEVICES_LIST = "ksdl";

    public static String KEY_IS_AUTO_CONNECT_BT = "kiacb";
    public static String KEY_LAST_CONNECTED_BT = "klcb";

    public static String KEY_USER_ACCOUNT = "kura";
    public static String KEY_USER_PASSWORD = "kurp";

    public static String KEY_NO_MORE_TIP_FOR_PROJECT_DELETE = "knmtfpd";
    public static String KEY_NO_MORE_TIP_FOR_SCANNER = "knmtfsc";
    public static String KEY_NO_MORE_TIP_FOR_SCAN_LIST = "knmtfsl";

    /**
     * 企业模式，0为在线模式需要登录，1为本地模式，不需要登录
     * */
    public static String KEY_COMPANY_MODE = "kusrmde";

    private InkConfig(){}

    public static int getCompanyMode(){
        return MMKV.defaultMMKV().decodeInt(KEY_COMPANY_MODE, 0);
    }

    public static void setCompanyMode(int mode){
        MMKV.defaultMMKV().encode(KEY_COMPANY_MODE, mode);
    }

    public static String getCacheCompanyCode(){
        return MMKV.defaultMMKV().decodeString(KEY_USER_COMPANY_CODE);
    }

    public static void setCacheCompanyCode(String code){
        MMKV.defaultMMKV().encode(KEY_USER_COMPANY_CODE, code);
    }

    public static String getCompanyExpired(){
        return MMKV.defaultMMKV().decodeString(KEY_USER_COMPANY_EXPIRED);
    }

    public static void setCompanyExpired(String expired){
        MMKV.defaultMMKV().encode(KEY_USER_COMPANY_EXPIRED, expired);
    }

    public static String getCompanyQRCodeUrl(){
        return MMKV.defaultMMKV().decodeString(KEY_COMPANY_QRCODE_URL);
    }

    public static void setCompanyQRCodeUrl(String qrCodeUrl){
        MMKV.defaultMMKV().encode(KEY_COMPANY_QRCODE_URL, qrCodeUrl);
    }

    public static String getCacheCompanyHost(){
        return MMKV.defaultMMKV().decodeString(KEY_COMPANY_HOST);
    }

    public static void setCacheCompanyHost(String host){
        MMKV.defaultMMKV().encode(KEY_COMPANY_HOST, host);
    }

    public static String getCacheCompanyName(){
        return MMKV.defaultMMKV().decodeString(KEY_COMPANY_NAME);
    }

    public static void setCacheCompanyName(String companyName){
        MMKV.defaultMMKV().encode(KEY_COMPANY_NAME, companyName);
    }

    public static String getCacheCompanyLogoUrl(){
        return MMKV.defaultMMKV().decodeString(KEY_COMPANY_LOGO_URL, "");
    }

    public static void setCacheCompanyLogoUrl(String logoUrl){
        MMKV.defaultMMKV().encode(KEY_COMPANY_LOGO_URL, logoUrl);
    }

    public static String getCacheCompanyBannerUrl(){
        return MMKV.defaultMMKV().decodeString(KEY_COMPANY_BANNER_URL, "");
    }

    public static void setCacheCompanyBannerUrl(String bannerUrl){
        MMKV.defaultMMKV().encode(KEY_COMPANY_BANNER_URL, bannerUrl);
    }

    public static void setExcelDataPath(String path){
        MMKV.defaultMMKV().encode(KEY_EXCEL_DATA_PATH, path);
    }

    public static String getExcelDataPath(){
        return MMKV.defaultMMKV().decodeString(KEY_EXCEL_DATA_PATH);
    }

    public static void setExcelSheetIndex(int sheetIndex){
        MMKV.defaultMMKV().encode(KEY_EXCEL_SHEET_INDEX, sheetIndex);
    }

    public static int getExcelSheetIndex(){
        return MMKV.defaultMMKV().decodeInt(KEY_EXCEL_SHEET_INDEX, 0);
    }

    public static boolean isAutoLogin(){
        return MMKV.defaultMMKV().decodeBool(KEY_IS_AUTO_LOGIN, true);
    }

    public static void setAutoLogin(boolean auto){
        MMKV.defaultMMKV().encode(KEY_IS_AUTO_LOGIN, auto);
    }

    public static void setNoMoreTipsForSelectBt(boolean showTip) {
        MMKV.defaultMMKV().encode(KEY_NO_MORE_TIPS_FOR_SELECT_BT, showTip);
    }

    public static boolean isNoMoreTipsForSelectBt() {
        return MMKV.defaultMMKV().decodeBool(KEY_NO_MORE_TIPS_FOR_SELECT_BT, false);
    }

    public static int getPrintFormat(){
        return MMKV.defaultMMKV().decodeInt(KEY_PRINT_FORMAT, InkConstant.PRINT_FORMAT_STANDARD);
    }

    public static void setPrintFormat(int format){
        MMKV.defaultMMKV().encode(KEY_PRINT_FORMAT, format);
    }

    public static int getEndCharacter(){
        return MMKV.defaultMMKV().decodeInt(KEY_END_CHARACTER, 13);
    }

    public static void setEndCharacter(int character){
        MMKV.defaultMMKV().encode(KEY_END_CHARACTER, character);
    }

    public static int getSplitCharacter(){
        return MMKV.defaultMMKV().decodeInt(KEY_SPLIT_CHARACTER, 9);
    }

    public static void setSplitCharacter(int character){
        MMKV.defaultMMKV().encode(KEY_SPLIT_CHARACTER, character);
    }

    public static void setPrintStartReturnValue(String value){
        MMKV.defaultMMKV().encode(KEY_PRINT_START_RETURN_VALUE, value);
    }

    public static String getPrintStartReturnValue(){
        return MMKV.defaultMMKV().decodeString(KEY_PRINT_START_RETURN_VALUE, "1");
    }

    public static void setPrintFinishReturnValue(String value){
        MMKV.defaultMMKV().encode(KEY_PRINT_FINISH_RETURN_VALUE, value);
    }

    public static String getPrintFinishReturnValue(){
        return MMKV.defaultMMKV().decodeString(KEY_PRINT_FINISH_RETURN_VALUE, "2");
    }

    public static void setPrintStopReturnValue(String value){
        MMKV.defaultMMKV().encode(KEY_PRINT_STOP_RETURN_VALUE, value);
    }

    public static String getPrintStopReturnValue(){
        return MMKV.defaultMMKV().decodeString(KEY_PRINT_STOP_RETURN_VALUE, "3");
    }

    public static boolean isNoMoreTip4Location(){
        return MMKV.defaultMMKV().decodeBool(KEY_NO_MORE_REQUEST_PERMISSION_TIP_FOR_LOCATION, false);
    }

    public static void setNoMoreTip4Location(boolean noTip){
        MMKV.defaultMMKV().encode(KEY_NO_MORE_REQUEST_PERMISSION_TIP_FOR_LOCATION, noTip);
    }

    public static int getUploadPrintRecordFlag(){
        return MMKV.defaultMMKV().decodeInt(KEY_IS_UPLOAD_PRINT_RECORD, InkConstant.NOT_UPLOAD_PRINT_RECORD);
    }

    public static void setUploadPrintRecordFlag(int flag){
        MMKV.defaultMMKV().encode(KEY_IS_UPLOAD_PRINT_RECORD, flag);
    }

    public static Set<String> getSupportDeviceList(){
        return MMKV.defaultMMKV().decodeStringSet(KEY_SUPPORT_DEVICES_LIST);
    }

    public static void setSupportDeviceList(Set<String> supportedDevices){
        if (supportedDevices == null){
            return;
        }

        MMKV.defaultMMKV().encode(KEY_SUPPORT_DEVICES_LIST, supportedDevices);
    }

    public static boolean isAutoConnectBt(){
        return MMKV.defaultMMKV().decodeBool(KEY_IS_AUTO_CONNECT_BT, true);
    }

    public static void setAutoConnectBt(boolean auto){
        MMKV.defaultMMKV().encode(KEY_IS_AUTO_CONNECT_BT, auto);
    }

    public static String getLastConnectedBt(){
        return MMKV.defaultMMKV().decodeString(KEY_LAST_CONNECTED_BT);
    }

    public static void setLastConnectedBt(String btMac){
        MMKV.defaultMMKV().encode(KEY_LAST_CONNECTED_BT, btMac);
    }

    public static String getCacheAccount(){
        return MMKV.defaultMMKV().decodeString(KEY_USER_ACCOUNT);
    }

    public static void setCacheAccount(String account){
        MMKV.defaultMMKV().encode(KEY_USER_ACCOUNT, account);
    }

    public static void setCachePassword(String password){
        MMKV.defaultMMKV().encode(KEY_USER_PASSWORD, password);
    }

    public static String getCachePassword(){
        return MMKV.defaultMMKV().decodeString(KEY_USER_PASSWORD);
    }

    public static boolean isNoMoreTip4ProjectDeleteDialog(){
        return MMKV.defaultMMKV().decodeBool(KEY_NO_MORE_TIP_FOR_PROJECT_DELETE, false);
    }

    public static boolean setNoMoreTip4ProjectDeleteDialog(boolean noMoreTip){
        return MMKV.defaultMMKV().encode(KEY_NO_MORE_TIP_FOR_PROJECT_DELETE, noMoreTip);
    }

    public static boolean isNoMoreTipForScanner() {
        return MMKV.defaultMMKV().decodeBool(KEY_NO_MORE_TIP_FOR_SCANNER, false);
    }

    public static void setNoMoreTipForScanner(boolean noMoreTipForScanner) {
        MMKV.defaultMMKV().encode(KEY_NO_MORE_TIP_FOR_SCANNER, noMoreTipForScanner);
    }

    public static boolean isNoMoreTipForScanList() {
        return MMKV.defaultMMKV().decodeBool(KEY_NO_MORE_TIP_FOR_SCAN_LIST, false);
    }

    public static void setNoMoreTipForScanList(boolean noMoreTipForScanList) {
        MMKV.defaultMMKV().encode(KEY_NO_MORE_TIP_FOR_SCAN_LIST, noMoreTipForScanList);
    }


    //喷码机相关
    private final  static String KEY_AUTO_WIFI = "katwf";
    private final  static String KEY_WIFI_IP = "kwi";
    private final  static String KEY_SPOT_SIZE = "kspsi";
    private final  static String KEY_RESOLUTION = "kreso";
    private final  static String KEY_REPEAT = "krept";
    private final  static String KEY_INTERVAL = "keyinter";
    private final  static String KEY_DISTANCE = "keydis";
    private final  static String KEY_PRESSURE = "keypres";
    private final  static String KEY_DIRECTION = "keydirr";
    private final  static String KEY_HORIZONTAL_FLIP = "khorfl";
    private final  static String KEY_VERTICAL_FLIP = "kverfl";
    private final  static String KEY_FONT_DISPLAY = "kefntdp";
    private final  static String KEY_FONT_PARAM = "kefntpm";
    private final  static String KEY_FONT_SIZE = "kfnts";
    private final  static String KEY_FONT_TYPE = "kfntyp";
    private final  static String KEY_LAST_WIFI_PRJ_NAME = "klwpn";

    private final  static String KEY_CONTINUOUS_PRINT = "kcp";
//    private final  static String KEY_FIXED_LENGTH = "kfl";

    public static void setContinuousPrint(boolean continuousPrint){
        MMKV.defaultMMKV().encode(KEY_CONTINUOUS_PRINT, continuousPrint);
    }

    public static boolean getContinuousPrint(){
        return MMKV.defaultMMKV().decodeBool(KEY_CONTINUOUS_PRINT, false);
    }

//    public static void setFixedLength(boolean fixedLength){
//        MMKV.defaultMMKV().encode(KEY_FIXED_LENGTH, fixedLength);
//    }
//
//    public static boolean getFixedLength(){
//        return MMKV.defaultMMKV().decodeBool(KEY_FIXED_LENGTH, false);
//    }

    public static void setFontType(int fontType){
        MMKV.defaultMMKV().encode(KEY_FONT_TYPE, fontType);
    }

    public static int getFontType(){
        return MMKV.defaultMMKV().decodeInt(KEY_FONT_TYPE, FontSectionEntity.TYPE_INTERNAL);
    }


    public static void setFontSize(int fontSize){
        MMKV.defaultMMKV().encode(KEY_FONT_SIZE, fontSize);
    }

    public static int getFontSize(){
        return MMKV.defaultMMKV().decodeInt(KEY_FONT_SIZE, 30);
    }

    public static String getLastWifiPrjName(){
        return MMKV.defaultMMKV().decodeString(KEY_LAST_WIFI_PRJ_NAME);
    }

    public static void setLastWifiPrjName(String prjName){
        MMKV.defaultMMKV().encode(KEY_LAST_WIFI_PRJ_NAME, prjName);
    }

    public static String getFontParam(){
        return MMKV.defaultMMKV().decodeString(KEY_FONT_PARAM, "Default/Font_32x24.xml");
    }

    public static void setFontParam(String fontParam){
        MMKV.defaultMMKV().encode(KEY_FONT_PARAM, fontParam);
    }

    public static String getDisplayFont(){
        return MMKV.defaultMMKV().decodeString(KEY_FONT_DISPLAY, "Font_32x24");
    }

    public static void setDisplayFont(String displayFont){
        MMKV.defaultMMKV().encode(KEY_FONT_DISPLAY, displayFont);
    }

    public static boolean isAutoWifi(){
        return MMKV.defaultMMKV().decodeBool(KEY_AUTO_WIFI, true);
    }

    public static void setAutoWifi(boolean autoWifi){
        MMKV.defaultMMKV().encode(KEY_AUTO_WIFI, autoWifi);
    }

    public static String getWifiIp(){
        return MMKV.defaultMMKV().decodeString(KEY_WIFI_IP, "192.168.0.100");
    }

    public static void setWifiIp(String ip){
        MMKV.defaultMMKV().encode(KEY_WIFI_IP, ip);
    }

    public static int getSpotSize(){
        return MMKV.defaultMMKV().decodeInt(KEY_SPOT_SIZE, 4);
    }

    public static void setSpotSize(int spotSize){
        MMKV.defaultMMKV().encode(KEY_SPOT_SIZE, spotSize);
    }

    public static void setResolution(int resolution){
        MMKV.defaultMMKV().encode(KEY_RESOLUTION, resolution);
    }

    public static int getResolution(){
        return MMKV.defaultMMKV().decodeInt(KEY_RESOLUTION, 550);
    }

    public static void setRepeat(int repeat){
        MMKV.defaultMMKV().encode(KEY_REPEAT, repeat);
    }

    public static int getRepeat(){
        return MMKV.defaultMMKV().decodeInt(KEY_REPEAT, 1);
    }

    public static void setInterval(int interval){
        MMKV.defaultMMKV().encode(KEY_INTERVAL, interval);
    }

    public static int getInterval(){
        return MMKV.defaultMMKV().decodeInt(KEY_INTERVAL, 0);
    }

    public static void setDistance(int distance){
        MMKV.defaultMMKV().encode(KEY_DISTANCE, distance);
    }

    public static int getDistance(){
        return MMKV.defaultMMKV().decodeInt(KEY_DISTANCE, 0);
    }

    public static void setPressure(int distance){
        MMKV.defaultMMKV().encode(KEY_PRESSURE, distance);
    }

    public static int getPressure(){
        return MMKV.defaultMMKV().decodeInt(KEY_PRESSURE, 35);
    }

    public static void setDirection(int direction){
        MMKV.defaultMMKV().encode(KEY_DIRECTION, direction);
    }

    public static int getDirection(){
        return MMKV.defaultMMKV().decodeInt(KEY_DIRECTION, 1);
    }

    public static void setHorizontalFlip(boolean isFlip){
        MMKV.defaultMMKV().encode(KEY_HORIZONTAL_FLIP, isFlip);
    }

    public static boolean getHorizontalFlip(){
        return MMKV.defaultMMKV().decodeBool(KEY_HORIZONTAL_FLIP, false);
    }

    public static void setVerticalFlip(boolean isFlip){
        MMKV.defaultMMKV().encode(KEY_VERTICAL_FLIP, isFlip);
    }

    public static boolean getVerticalFlip(){
        return MMKV.defaultMMKV().decodeBool(KEY_VERTICAL_FLIP, false);
    }
}
