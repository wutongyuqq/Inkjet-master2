package me.samlss.inkjet.bean;

import java.util.List;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description 数据分隔基类
 */
public class SplitDataListBean {
    public int index;
    public List<String> dataList;

    public SplitDataListBean(int index, List<String> dataList) {
        this.index = index;
        this.dataList = dataList;
    }
}
