package com.yzi.doutu.service;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.android.bmoblibrary.Library;
import com.yzi.doutu.BuildConfig;
import com.yzi.doutu.utils.CommUtil;
import com.zhy.http.okhttp.OkHttpUtils;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class DouApplication extends Application {
    private static DouApplication application;
    private Context mContext;

    private ArrayList<Activity> activities = null; // 存储浏览过的Activity

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        Library.init(this, BuildConfig.BMOB_KEY, BuildConfig.DEBUG);
        initOk();
        CommUtil.getInstance().startService();
    }

    private void initOk() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                .readTimeout(10000L, TimeUnit.MILLISECONDS)
                //其他配置
                .build();

        OkHttpUtils.initClient(okHttpClient);
    }

    public static DouApplication getInstance() {
        if (application == null) {
            application = new DouApplication();
        }
        return application;
    }

    public Context getContext() {
        if (mContext == null) {
            mContext = getApplicationContext();
        }
        return mContext;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        CommUtil.getInstance().stopService();
    }

    /*
     * 将浏览过的activity储存起来
	 */
    public void addActivity(Activity activity) {
        if (activities == null)
            activities = new ArrayList<>();
        activities.add(activity);
    }

    /**
     * 结束所有acitvity
     */
    public void removeAllActivity() {
        if (activities != null) {
            for (Activity activity : activities) {
                if (!activity.isFinishing()) {
                    activity.finish();
                }
            }
            activities.clear();
            activities = null;
        }
    }

}
