package cn.elbereth.sinaweibodemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.weibo.sdk.auth.AccessTokenKeeper;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WbAuthListener;
import com.sina.weibo.sdk.auth.WbConnectErrorMessage;
import com.sina.weibo.sdk.auth.sso.SsoHandler;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author Kang S.
 * @author shoukang@yeecall.com
 *         Created on 17/8/1.
 */

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private static final ExecutorService POOL = Executors.newCachedThreadPool();

    private SsoHandler mSsoHandler;
    private TextView mTokenTv;
    private Oauth2AccessToken mToken;
    private OkHttpClient mClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSsoHandler = new SsoHandler(this);
        mClient = new OkHttpClient();
        mTokenTv = (TextView) findViewById(R.id.main_tv_token);

        findViewById(R.id.main_btn_login).setOnClickListener(v -> mSsoHandler.authorize(new SelfWbAuthListener()));
        findViewById(R.id.main_btn_info).setOnClickListener(v -> getInfo());
        findViewById(R.id.main_btn_verify).setOnClickListener(v -> verifyToken());

        mToken = AccessTokenKeeper.readAccessToken(getApplication());
        showToken();
    }

    private void showToast(String msg) {
        Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show();
        Log.i(TAG, "showToast: " + msg);
    }

    @SuppressLint("SetTextI18n")
    private void showToken() {
        if (isTokenEmpty()) {
            mTokenTv.setText("No token");
        } else {
            mTokenTv.setText(String.format("uid: %s\naccess token: %s\nrefresh token: %s\nexpires time: %s",
                    mToken.getUid(), mToken.getToken(), mToken.getRefreshToken(), new Date(mToken.getExpiresTime())));
        }
    }

    private boolean isTokenEmpty() {
        return mToken == null || TextUtils.isEmpty(mToken.getUid()) || TextUtils.isEmpty(mToken.getToken());
    }

    private static final String INFO_URL = "https://api.weibo.com/2/users/show.json";
    private static final String VERIFY_URL = "https://api.weibo.com/oauth2/get_token_info";

    private void getInfo() {
        if (isTokenEmpty()) {
            showToast("Request token first");
            return;
        }
        POOL.execute(() -> {
            HttpUrl url = HttpUrl.parse(INFO_URL);
            if (url == null) {
                Log.e(TAG, "getInfo: wrong url = " + INFO_URL);
                return;
            }

            url = url.newBuilder().addQueryParameter("uid", mToken.getUid())
                    .addQueryParameter("access_token", mToken.getToken()).build();
            Request request = new Request.Builder().url(url).build();
            try {
                Response response = mClient.newCall(request).execute();
                //noinspection ConstantConditions
                Log.i(TAG, "getInfo: " + response.body().string());
            } catch (IOException | NullPointerException e) {
                Log.e(TAG, "getInfo: failed to get info", e);
            }
        });
    }

    private void verifyToken() {
        if (isTokenEmpty()) {
            showToast("Request token first");
            return;
        }
        POOL.execute(() -> {
            RequestBody body = new FormBody.Builder().add("access_token", mToken.getToken()).build();
            Request request = new Request.Builder().url(VERIFY_URL).post(body).build();
            try {
                Response response = mClient.newCall(request).execute();
                //noinspection ConstantConditions
                Log.i(TAG, "verifyToken: " + response.body().string());
            } catch (IOException | NullPointerException e) {
                Log.e(TAG, "verifyToken: failed to get info", e);
            }
        });
    }

    private class SelfWbAuthListener implements WbAuthListener {

        @Override
        public void onSuccess(Oauth2AccessToken oauth2AccessToken) {
            AccessTokenKeeper.writeAccessToken(getApplication(), oauth2AccessToken);
            runOnUiThread(() -> {
                mToken = oauth2AccessToken;
                showToken();
            });
        }

        @Override
        public void cancel() {
            showToast("Weibo login cancel");
        }

        @Override
        public void onFailure(WbConnectErrorMessage wbConnectErrorMessage) {
            showToast(wbConnectErrorMessage.getErrorCode() + " : " + wbConnectErrorMessage.getErrorMessage());
        }
    }
}
