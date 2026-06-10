package tool.xfy9326.floatpicture.Methods;


import static tool.xfy9326.floatpicture.Methods.WindowsMethods.getWindowManager;

import android.app.Activity;
import android.app.AppOpsManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Process;
import android.view.View;
import android.view.WindowManager;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import tool.xfy9326.floatpicture.MainApplication;
import tool.xfy9326.floatpicture.Utils.Config;
import tool.xfy9326.floatpicture.Utils.PictureData;
import tool.xfy9326.floatpicture.View.FloatImageView;


public class ManageMethods {

    public static void SelectPicture(Activity mActivity) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        mActivity.startActivityForResult(intent, Config.REQUEST_CODE_ACTIVITY_PICTURE_SETTINGS_GET_PICTURE);
    }

    public static void RunWin(Context mContext) {
        if (PermissionMethods.checkPermission(mContext, PermissionMethods.StoragePermission)) {
            PictureData pictureData = new PictureData();
            LinkedHashMap<String, String> list = pictureData.getListArray();
            WindowManager windowManager = getWindowManager(mContext);
            if (!list.isEmpty()) {
                for (LinkedHashMap.Entry<?, ?> entry : list.entrySet()) {
                    StartWin(mContext, windowManager, pictureData, entry.getKey().toString());
                }
            }
        }
    }

    private static void StartWin(Context mContext, WindowManager windowManager, PictureData pictureData, String id) {
        pictureData.setDataControl(id);
        Bitmap bitmap = ImageMethods.getShowBitmap(mContext, id);
        float default_zoom = pictureData.getFloat(Config.DATA_PICTURE_DEFAULT_ZOOM, ImageMethods.getDefaultZoom(mContext, bitmap, false));
        float zoom = pictureData.getFloat(Config.DATA_PICTURE_ZOOM, default_zoom);
        float picture_degree = pictureData.getFloat(Config.DATA_PICTURE_DEGREE, Config.DATA_DEFAULT_PICTURE_DEGREE);
        float picture_alpha = pictureData.getFloat(Config.DATA_PICTURE_ALPHA, Config.DATA_DEFAULT_PICTURE_ALPHA);
        int position_x = pictureData.getInt(Config.DATA_PICTURE_POSITION_X, Config.DATA_DEFAULT_PICTURE_POSITION_X);
        int position_y = pictureData.getInt(Config.DATA_PICTURE_POSITION_Y, Config.DATA_DEFAULT_PICTURE_POSITION_Y);
        boolean touch_and_move = pictureData.getBoolean(Config.DATA_PICTURE_TOUCH_AND_MOVE, Config.DATA_DEFAULT_PICTURE_TOUCH_AND_MOVE);
        boolean over_layout = pictureData.getBoolean(Config.DATA_ALLOW_PICTURE_OVER_LAYOUT, Config.DATA_DEFAULT_ALLOW_PICTURE_OVER_LAYOUT);
        boolean fill_screen = pictureData.getBoolean(Config.DATA_PICTURE_FILL_SCREEN, Config.DATA_DEFAULT_PICTURE_FILL_SCREEN);
        FloatImageView floatImageView = ImageMethods.createPictureView(mContext, bitmap, touch_and_move, over_layout, zoom, picture_degree);
        if (fill_screen) {
            floatImageView.setFillScreen(true);
            floatImageView.setImageBitmap(bitmap);
        }
        boolean filter_app_enabled = pictureData.getBoolean(Config.DATA_PICTURE_FILTER_APP_ENABLED, Config.DATA_DEFAULT_PICTURE_FILTER_APP_ENABLED);
        String filter_app_package = pictureData.getString(Config.DATA_PICTURE_FILTER_APP_PACKAGE, Config.DATA_DEFAULT_PICTURE_FILTER_APP_PACKAGE);
        floatImageView.setFilterAppEnabled(filter_app_enabled);
        floatImageView.setFilterAppPackage(filter_app_package);
        floatImageView.setAlpha(picture_alpha);
        ImageMethods.saveFloatImageViewById(mContext, id, floatImageView);
        if (pictureData.getBoolean(Config.DATA_PICTURE_SHOW_ENABLED, Config.DATA_DEFAULT_PICTURE_SHOW_ENABLED)) {
            WindowsMethods.createWindow(windowManager, floatImageView, touch_and_move, over_layout, position_x, position_y);
        }
    }

    public static void DeleteWin(Context mContext, String id) {
        PictureData pictureData = new PictureData();
        pictureData.setDataControl(id);
        if (pictureData.getBoolean(Config.DATA_PICTURE_SHOW_ENABLED, Config.DATA_DEFAULT_PICTURE_SHOW_ENABLED)) {
            FloatImageView floatImageView = ImageMethods.getFloatImageViewById(mContext, id);
            if (floatImageView != null) {
                WindowsMethods.safeRemoveView(getWindowManager(mContext), floatImageView);
                floatImageView.refreshDrawableState();
            }
        }
        pictureData.remove();
        ImageMethods.clearAllTemp(mContext, id);
    }

    static void CloseAllWindows(Context mContext) {
        HashMap<String, View> hashMap = ((MainApplication) mContext.getApplicationContext()).getRegister();
        WindowManager windowManager = getWindowManager(mContext);
        PictureData pictureData = new PictureData();
        if (!hashMap.isEmpty()) {
            for (HashMap.Entry<?, ?> entry : hashMap.entrySet()) {
                pictureData.setDataControl(entry.getKey().toString());
                if (pictureData.getBoolean(Config.DATA_PICTURE_SHOW_ENABLED, Config.DATA_DEFAULT_PICTURE_SHOW_ENABLED)) {
                    FloatImageView floatImageView = (FloatImageView) entry.getValue();
                    WindowsMethods.safeRemoveView(windowManager, floatImageView);
                    floatImageView.refreshDrawableState();
                }
            }
        }
    }

    public static void updateNotificationCount(Context context) {
        context.sendBroadcast(new Intent().setAction(Config.INTENT_ACTION_NOTIFICATION_UPDATE_COUNT));
    }

    public static void setAllWindowsVisible(Context context, boolean visible) {
        String id;
        PictureData pictureData = new PictureData();
        LinkedHashMap<String, String> linkedHashMap = pictureData.getListArray();
        for (Map.Entry<?, ?> o : linkedHashMap.entrySet()) {
            id = o.getKey().toString();
            setWindowVisible(context, pictureData, id, visible);
        }
    }

    public static void setWindowVisible(Context context, PictureData pictureData, String id, boolean visible) {
        pictureData.setDataControl(id);
        boolean data_visible = pictureData.getBoolean(Config.DATA_PICTURE_SHOW_ENABLED, visible);
        if (visible) {
            if (!data_visible) {
                pictureData.put(Config.DATA_PICTURE_SHOW_ENABLED, true);
                pictureData.commit(null);
                if (shouldShowFilteredWindow(context, pictureData, id)) {
                    showWindowById(context, id);
                }
            }
        } else {
            if (data_visible) {
                hideWindowById(context, id);
                pictureData.put(Config.DATA_PICTURE_SHOW_ENABLED, false);
                pictureData.commit(null);
            }
        }
    }

    private static boolean shouldShowFilteredWindow(Context context, PictureData pictureData, String id) {
        boolean filterEnabled = pictureData.getBoolean(Config.DATA_PICTURE_FILTER_APP_ENABLED, Config.DATA_DEFAULT_PICTURE_FILTER_APP_ENABLED);
        if (!filterEnabled) return true;
        if (!hasUsageStatsPermission(context)) return true;
        String filterPackage = pictureData.getString(Config.DATA_PICTURE_FILTER_APP_PACKAGE, Config.DATA_DEFAULT_PICTURE_FILTER_APP_PACKAGE);
        if (filterPackage.isEmpty()) return true;
        String fgPkg = getForegroundPackage(context);
        return filterPackage.equals(fgPkg);
    }

    private static void hideWindowById(Context mContext, String id) {
        FloatImageView floatImageView = ImageMethods.getFloatImageViewById(mContext, id);
        WindowsMethods.safeRemoveView(getWindowManager(mContext), floatImageView);
    }

    static void showWindowById(Context mContext, String id) {
        FloatImageView floatImageView = ImageMethods.getFloatImageViewById(mContext, id);
        PictureData pictureData = new PictureData();
        pictureData.setDataControl(id);
        int positionX = pictureData.getInt(Config.DATA_PICTURE_POSITION_X, Config.DATA_DEFAULT_PICTURE_POSITION_X);
        int positionY = pictureData.getInt(Config.DATA_PICTURE_POSITION_Y, Config.DATA_DEFAULT_PICTURE_POSITION_Y);
        boolean touch_and_move = pictureData.getBoolean(Config.DATA_PICTURE_TOUCH_AND_MOVE, Config.DATA_DEFAULT_PICTURE_TOUCH_AND_MOVE);
        boolean over_layout = pictureData.getBoolean(Config.DATA_ALLOW_PICTURE_OVER_LAYOUT, Config.DATA_DEFAULT_ALLOW_PICTURE_OVER_LAYOUT);
        WindowManager.LayoutParams layoutParams = WindowsMethods.getDefaultLayout(floatImageView, positionX, positionY, touch_and_move, over_layout);
        if (floatImageView.isAttachedToWindow()) {
            getWindowManager(mContext).updateViewLayout(floatImageView, layoutParams);
        } else {
            getWindowManager(mContext).addView(floatImageView, layoutParams);
        }
    }

    public static String getForegroundPackage(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return null;
        UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        if (usm == null) return null;
        long endTime = System.currentTimeMillis();
        long beginTime = endTime - 60000;
        UsageEvents events = usm.queryEvents(beginTime, endTime);
        String foregroundPkg = null;
        long lastTimeStamp = 0;
        UsageEvents.Event event = new UsageEvents.Event();
        while (events.hasNextEvent()) {
            events.getNextEvent(event);
            if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                if (event.getTimeStamp() > lastTimeStamp) {
                    lastTimeStamp = event.getTimeStamp();
                    foregroundPkg = event.getPackageName();
                }
            }
        }
        return foregroundPkg;
    }

    public static boolean hasUsageStatsPermission(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        if (appOps == null) return false;
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(), context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    public static void updateWindowsForForegroundApp(Context context, String foregroundPackage) {
        MainApplication mainApplication = (MainApplication) context.getApplicationContext();
        HashMap<String, View> views = mainApplication.getRegister();
        PictureData pictureData = new PictureData();
        for (Map.Entry<String, View> entry : views.entrySet()) {
            String id = entry.getKey();
            View view = entry.getValue();
            if (view instanceof FloatImageView) {
                FloatImageView floatImageView = (FloatImageView) view;
                if (floatImageView.isFilterAppEnabled()) {
                    String filterPackage = floatImageView.getFilterAppPackage();
                    boolean shouldShow = filterPackage.equals(foregroundPackage);
                    pictureData.setDataControl(id);
                    boolean userWantsShown = pictureData.getBoolean(Config.DATA_PICTURE_SHOW_ENABLED, Config.DATA_DEFAULT_PICTURE_SHOW_ENABLED);
                    boolean isAttached = floatImageView.isAttachedToWindow();
                    if (shouldShow && userWantsShown && !isAttached && mainApplication.getWinVisible()) {
                        showWindowById(context, id);
                    } else if (!shouldShow && isAttached) {
                        hideWindowById(context, id);
                    }
                }
            }
        }
    }

}
