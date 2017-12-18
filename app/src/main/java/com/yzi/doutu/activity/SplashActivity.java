package com.yzi.doutu.activity;

import android.content.Intent;

import com.android.bmoblibrary.ui.SplashBaseActivity;
import com.android.bmoblibrary.utils.ResourceUtil;
import com.yzi.doutu.BuildConfig;
import com.yzi.doutu.main.MainActivity;

/**
 * author: Rea.X
 * date: 2017/12/18.
 */

public class SplashActivity extends SplashBaseActivity{
    @Override
    protected void toMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    protected int getSplashImageRes() {
        return ResourceUtil.getDrawableId(this, BuildConfig.SPLASH_PIC);
    }
}
