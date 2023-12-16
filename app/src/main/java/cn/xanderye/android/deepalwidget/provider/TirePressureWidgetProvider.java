package cn.xanderye.android.deepalwidget.provider;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.xanderye.android.deepalwidget.R;
import cn.xanderye.android.deepalwidget.constant.Constants;
import cn.xanderye.android.deepalwidget.entity.CarData;
import cn.xanderye.android.deepalwidget.service.DeepalService;

/**
 * @author yezhendong
 * @description:
 * @date 2023/3/16 9:59
 */
public class TirePressureWidgetProvider extends AppWidgetProvider {

    private static final String TAG = TirePressureWidgetProvider.class.getSimpleName();

    public static final String OPEN_DEEPAL_WIDGET = "cn.xanderye.android.OPEN_DEEPAL_WIDGET";

    @Override
    public void onEnabled(Context context) {
        Log.d(TAG, "widget onEnabled");
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        Log.d(TAG, "widget onDisabled");
        super.onDisabled(context);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        Log.d(TAG, "widget onDeleted");
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "widget onReceive, action: " + action);
        switch (action) {
            case OPEN_DEEPAL_WIDGET:
                PackageManager packageManager = context.getPackageManager();
                Intent appIntent = packageManager.getLaunchIntentForPackage(Constants.DEEPAL_PACKAGE_NAME);
                if (appIntent != null) {
                    try {
                        context.startActivity(appIntent);
                    } catch (Exception e) {
                        Log.d(TAG, "深蓝小部件设置打开失败，原因：" + e.getMessage());
                        Toast.makeText(context, "打开失败", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case AppWidgetManager.ACTION_APPWIDGET_UPDATE:
                Log.d(TAG, "触发系统刷新");
                break;
        }
        RemoteViews remoteViews = bindButton(context);
        refreshWidget(context, remoteViews);
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "widget onUpdate");

        for (int appWidgetId : appWidgetIds) {
            Log.d(TAG, "widget id: " + appWidgetId);
            RemoteViews remoteViews = bindButton(context);
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    public static RemoteViews bindButton(Context context) {
        int flag;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            flag = PendingIntent.FLAG_IMMUTABLE;
        } else {
            flag = PendingIntent.FLAG_UPDATE_CURRENT;
        }
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.car_tire_pressure_widget);
        Intent appIntent = new Intent(context, TirePressureWidgetProvider.class).setAction(OPEN_DEEPAL_WIDGET);
        PendingIntent appPendingIntent = PendingIntent.getBroadcast(context, 0, appIntent, flag);
        remoteViews.setOnClickPendingIntent(R.id.tire_car_img, appPendingIntent);
        return remoteViews;
    }

    public static boolean getCarData(CarData carData, Context context, RemoteViews remoteViews) {
        if (carData == null) {
            DeepalService deepalService = DeepalService.getInstance();
            deepalService.setContext(context);
            carData = deepalService.getCarData();
        }
        if (carData != null) {
            Log.d(TAG, "胎压信息：" + carData);
            remoteViews.setTextViewText(R.id.lftiretv2, (float)carData.getLfTyrePressure()/100 + "");
            remoteViews.setTextViewText(R.id.lrtiretv2, (float)carData.getLrTyrePressure()/100 + "");
            remoteViews.setTextViewText(R.id.rftiretv2, (float)carData.getRfTyrePressure()/100 + "");
            remoteViews.setTextViewText(R.id.rrtiretv2, (float)carData.getRrTyrePressure()/100 + "");
            return true;
        } else {
            Log.d(TAG, "车辆信息为null");
        }
        return false;
    }

    private void refreshWidget(Context context, RemoteViews remoteViews) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName componentName = new ComponentName(context, TirePressureWidgetProvider.class);
        appWidgetManager.updateAppWidget(componentName, remoteViews);
    }
}