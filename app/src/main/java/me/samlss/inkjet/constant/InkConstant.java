package me.samlss.inkjet.constant;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description 常量类
 */
public class InkConstant {
    private InkConstant(){}

    public static final int INK_STATE_SPRAYED = 1; //已喷墨
    public static final int INK_STATE_NOT_SPRAYED = 2; //未喷墨

    public static final int ANNAL_TYPE_ALL = 0;
    public static final int ANNAL_TYPE_SPRAYED = 1;
    public static final int ANNAL_TYPE_NOT_SPRAYED = 2;

    public static final int PRINT_FORMAT_STANDARD = 0;
    public static final int PRINT_FROAMT_HEX = 1;

    public static final int PRINT_STATE_NONE = 0;
    public static final int PRINT_STATE_PRINTING = 1;
    public static final int PRINT_STATE_FINISH = 2;

    public final static int RL_ACTION_REFRESH = 0;
    public final static int RL_ACTION_LOADMORE = 1;

    public final static int UPLOAD_PRINT_RECORD = 1;
    public final static int NOT_UPLOAD_PRINT_RECORD = 0;
}
