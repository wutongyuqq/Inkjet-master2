package me.samlss.inkjet.ui.adapters;

import com.chad.library.adapter.base.entity.SectionEntity;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description
 */
public class FontSectionEntity extends SectionEntity<String> {
    public static final int TYPE_INTERNAL = 1;
    public static final int TYPE_USER = 2;
    public static final int TYPE_SYSTEM_DEFAULT = 3;

    public int type;
    private static final long serialVersionUID = -5701955725389688271L;

    public FontSectionEntity(boolean isHeader, String header, String content, int type) {
        super(isHeader, header);
        this.t = content;
        this.type = type;
    }
}
