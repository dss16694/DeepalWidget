package cn.xanderye.android.deepalwidget.service;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.xanderye.android.deepalwidget.entity.CarData;
import cn.xanderye.android.deepalwidget.provider.CarWidgetProvider;

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
                manager.updateAppWidget(new ComponentName(mContext, CarWidgetProvider.class), carDataRemoteViews);
            } else {
                msg = "刷新失败，请检查配置";
            }
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            Looper.loop();
        });
        singleThreadExecutor.shutdown();
    }

}