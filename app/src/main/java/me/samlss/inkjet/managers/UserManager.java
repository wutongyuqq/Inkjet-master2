package me.samlss.inkjet.managers;

import me.samlss.framework.utils.EncodeUtils;
import me.samlss.inkjet.config.InkConfig;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description 用户管理器
 */
public class UserManager {
    private static UserManager sInstance = new UserManager();

    private String userId = "tonglide";

    private UserManager(){

    }

    public static UserManager getInstance() {
        return sInstance;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCompanyUserId(){
        return new String(EncodeUtils.base64Encode(InkConfig.getCacheCompanyCode() + userId));
    }
}
