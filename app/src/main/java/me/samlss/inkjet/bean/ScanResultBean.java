package me.samlss.inkjet.bean;

import androidx.annotation.NonNull;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description 扫描条码后  服务器取回来的数据
 */
public class ScanResultBean {
    private String key;
    private String value;

    public ScanResultBean(){

    }

    public ScanResultBean(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("key = %s, value = %s", key, value);
    }
}