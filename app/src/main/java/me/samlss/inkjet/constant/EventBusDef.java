package me.samlss.inkjet.constant;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description
 */
public class EventBusDef {
    public static final int FLAG_UPDATE_HEADER = 1;
    public static final int FLAG_QRCODE_RESULT = 2;


    public static class BusBean{
        public int flag;
        public Object tag;

        public BusBean(){

        }

        public BusBean(int flag, Object tag){
            this.flag = flag;
            this.tag = tag;
        }
    }
}
