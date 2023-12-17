package cn.xanderye.android.deepalwidget.service;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.xanderye.android.deepalwidget.constant.Constants;
import cn.xanderye.android.deepalwidget.entity.CarData;
import cn.xanderye.android.deepalwidget.provider.CarWidgetProvider;
import cn.xanderye.android.deepalwidget.provider.TirePressureWidgetProvider;
import cn.xanderye.android.deepalwidget.util.DeepalUtil;

public class CarWidgetService extends Service {
    private static final long INTERVAL = 1000*60*1; // 更新间隔，以毫秒为单位
    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d("sunx","刷新界面");
            updateWidget(); // 执行更新小部件的操作
            mHandler.postDelayed(this, INTERVAL);
        }
    };
    @Override
    public void onCreate() {
        super.onCreate();
        mHandler.post(mRunnable);
        updateWidget();

    }

    public CarWidgetService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    private void updateWidget(){
        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
        singleThreadExecutor.execute(() -> {
            Looper.prepare();
            Context mContext = getApplicationContext();
            DeepalService deepalService = DeepalService.getInstance();
            deepalService.setContext(this);
            CarData carData = deepalService.getCarData();
            String msg = "刷新成功";
            if (carData != null) {
                AppWidgetManager manager = AppWidgetManager.getInstance(mContext);
                RemoteViews carDataRemoteViews = CarWidgetProvider.bindButton(mContext);
                CarWidgetProvider.getCarData(carData, mContext, carDataRemoteViews);
                RemoteViews tirePresureRemoteViews = TirePressureWidgetProvider.bindButton(mContext);
                TirePressureWidgetProvider.getCarData(carData, mContext, tirePresureRemoteViews);
                manager.updateAppWidget(new ComponentName(mContext, CarWidgetProvider.class), carDataRemoteViews);
                manager.updateAppWidget(new ComponentName(mContext, TirePressureWidgetProvider.class), tirePresureRemoteViews);
            } else {
                msg = "刷新失败，请检查配置";
                SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(mContext);
                String finalToken = config.getString(Constants.TOKEN_KEY,"");
                SharedPreferences.Editor edit = config.edit();
                String deviceId = DeepalUtil.uuid(36);
                String key = UUID.randomUUID().toString();
                DeepalUtil.setConfig(deviceId, key);
                if (finalToken != null) {
                    try {
                        DeepalUtil.sessionKeyRetry(finalToken, key);
                        edit.putString(Constants.SESSION_KEY_KEY,key);
                        edit.putString(Constants.DEVICE_ID_KEY,deviceId);
                        edit.apply();
                    } catch (Exception e) {
                        Toast.makeText(this, "提交会话密钥失败", Toast.LENGTH_SHORT).show();
                        return;
                    }

                }
            }
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            Looper.loop();
        });
        singleThreadExecutor.shutdown();
    }

}