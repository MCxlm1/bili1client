package com.RobinNotBad.BiliClient;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

public class BiliClient extends Application {
    @SuppressLint("StaticFieldLeak")
    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        if(context==null && getApplicationContext()!=null){
            context = getApplicationContext();
            ErrorCatch errorCatch = ErrorCatch.getInstance();
            errorCatch.init(getApplicationContext());
            SharedPreferencesUtil.initSharedPrefs(this);
        }
    }

    /**
     * 重写attachBaseContext方法，用于调整应用内dpi
     * 尝试下这种风格代码是否会导致低版本设备异常
     * @param old The origin context.
     */
    public static Context getFitDisplayContext(Context old){
        Context newContext = old;
        float dpiTimes = SharedPreferencesUtil.getFloat("dpi", 1.0F);
        if(dpiTimes == 1.0F) return newContext;
        try{
            DisplayMetrics displayMetrics = old.getResources().getDisplayMetrics();
            displayMetrics.densityDpi = (int) (displayMetrics.densityDpi * dpiTimes);
            Configuration configuration = old.getResources().getConfiguration();
            configuration.densityDpi = displayMetrics.densityDpi;
            newContext = old.createConfigurationContext(configuration);
        }catch (Exception e){
            Toast.makeText(newContext, "调整缩放失败, 请联系开发者", Toast.LENGTH_SHORT).show();
            Log.wtf("调整dpi", "不支持application");
        }
        return newContext;
    }
}
