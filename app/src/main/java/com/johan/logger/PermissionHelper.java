package com.johan.logger;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by johan on 2018/4/2.
 *
     CALENDAR（日历）
         READ_CALENDAR
         WRITE_CALENDAR
     CAMERA（相机）
        CAMERA
     CONTACTS（联系人）
        READ_CONTACTS
        WRITE_CONTACTS
        GET_ACCOUNTS
     LOCATION（位置）
        ACCESS_FINE_LOCATION
        ACCESS_COARSE_LOCATION
     MICROPHONE（麦克风）
        RECORD_AUDIO
     PHONE（手机）
        READ_PHONE_STATE
        CALL_PHONE
        READ_CALL_LOG
        WRITE_CALL_LOG
        ADD_VOICEMAIL
        USE_SIP
        PROCESS_OUTGOING_CALLS
     SENSORS（传感器）
        BODY_SENSORS
     SMS（短信）
        SEND_SMS
        RECEIVE_SMS
        READ_SMS
        RECEIVE_WAP_PUSH
        RECEIVE_MMS
     STORAGE（存储卡）
        READ_EXTERNAL_STORAGE
        WRITE_EXTERNAL_STORAGE
 *
 */

public class PermissionHelper {

    // 默认申请权限请求码
    public static final int DEFAULT_REQUEST_CODE = 888;

    /**
     * 申请权限
     * @param activity
     * @param permissions
     * @param callback
     */
    public static void requestPermission(Activity activity, String[] permissions, OnPermissionCallback callback) {
        requestPermission(activity, DEFAULT_REQUEST_CODE, permissions, callback);
    }

    /**
     * 申请权限
     * @param activity
     * @param requestCode
     * @param permissions
     * @param callback
     */
    public static void requestPermission(Activity activity, int requestCode, String[] permissions, OnPermissionCallback callback) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (callback != null) {
                callback.onPermissionAccept(requestCode, permissions);
            }
        }
        String[] requestPermissions = checkPermissions(activity, permissions);
        if (requestPermissions == null || requestPermissions.length == 0) {
            if (callback != null) {
                callback.onPermissionAccept(requestCode, permissions);
            }
            return;
        }
        ActivityCompat.requestPermissions(activity, requestPermissions, requestCode);
    }

    /**
     * 处理申请权限结果
     * @param requestCode
     * @param permissions
     * @param grantResults
     * @param callback
     */
    public static void handlePermissionResult(int requestCode, String permissions[], int[] grantResults, OnPermissionCallback callback) {
        List<String> refusePermissionList = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                refusePermissionList.add(permissions[i]);
            }
        }
        if (refusePermissionList.size() == 0) {
            if (callback != null) {
                callback.onPermissionAccept(requestCode, permissions);
            }
            return;
        }
        if (callback != null) {
            callback.onPermissionRefuse(requestCode, refusePermissionList.toArray(new String[refusePermissionList.size()]));
        }
    }

    /**
     * 检查权限
     * @param activity
     * @param permissions
     * @return
     */
    private static String[] checkPermissions(Activity activity, String... permissions) {
        List<String> permissionList = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permission);
            }
        }
        return permissionList.toArray(new String[permissionList.size()]);
    }

    /**
     * 处理权限回调
     */
    public interface OnPermissionCallback {

        /**
         * 接受
         * @param requestCode
         * @param permissions
         */
        void onPermissionAccept(int requestCode, String... permissions);

        /**
         * 拒绝
         * @param requestCode
         * @param permission
         */
        void onPermissionRefuse(int requestCode, String... permission);

    }

}
