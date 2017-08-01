package cn.elbereth.sinaweibodemo;

import android.app.Application;

import com.sina.weibo.sdk.WbSdk;
import com.sina.weibo.sdk.auth.AuthInfo;

/**
 * @author Kang S.
 * @author shoukang@yeecall.com
 *         Created on 17/8/1.
 */

public class MyApplication extends Application {
    public static final String APP_KEY = "2045436852";
    public static final String REDIRECT_URL = "http://www.sina.com";
    public static final String SCOPE = "email,direct_messages_read,direct_messages_write,"
            + "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
            + "follow_app_official_microblog," + "invitation_write";

    @Override
    public void onCreate() {
        super.onCreate();
        WbSdk.install(this, new AuthInfo(this, APP_KEY, REDIRECT_URL, SCOPE));
    }
}
