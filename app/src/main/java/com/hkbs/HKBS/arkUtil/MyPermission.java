package com.hkbs.HKBS.arkUtil;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;

import com.hkbs.HKBS.R;

import org.arkist.share.AxTools;

/**
 * Created by dchow on 26/1/2016.
 */
public class MyPermission {
    static final public int REQUEST_ACCESS_STORAGE = 1;
    static final public int REQUEST_ACCESS_LOCATION = 2;
    static final public int REQUEST_ACCESS_CALENDAR = 3;
    static final public int REQUEST_ACCESS_SYNC = 4;
    static final public int REQUEST_ACCESS_ACCOUNT_CAMERA = 5;
    static final public int REQUEST_ACCESS_CONFIG = 6;
    static final public int REQUEST_ACCESS_ALARM = 7;

    static private MyPermission myPermission;
    public interface Callback{
        public void onRequestResult(int requestCode, boolean success);
    }
    static public MyPermission getInstance(){
        if (myPermission==null){
            myPermission=new MyPermission();
        }
        return myPermission;
    }
    public void MyPermission() {
    }
    public boolean checkPermission(final Context context, final int requestCode, boolean suppressRequestPermission ){
        final Activity activity = (context instanceof Activity)?(Activity)context:null;
        if (Build.VERSION.SDK_INT >= 23) {
            //减少是否拥有权限
            int permission=0;
            final String [] permissionArr;
            String permission_1="";
            String permission_2="";
            String permissionDesc;
            switch (requestCode){
                case REQUEST_ACCESS_ACCOUNT_CAMERA:
                    permissionDesc=context.getString(R.string.permissionCalendar) + "," +
                            context.getString(R.string.permissionContact);
                    permission_1 = Manifest.permission.CAMERA;
                    permission_2 = Manifest.permission.GET_ACCOUNTS;
                    permission = ContextCompat.checkSelfPermission(context, permission_1);
                    if (permission== PackageManager.PERMISSION_GRANTED) {
                        permission = ContextCompat.checkSelfPermission(context, permission_2);
                    }
                    permissionArr = new String[]{
                            Manifest.permission.CAMERA,
                            Manifest.permission.GET_ACCOUNTS,
                            Manifest.permission.READ_CONTACTS};

                    break;
                case REQUEST_ACCESS_STORAGE:
                    permissionDesc=context.getString(R.string.permissionStorage);
                    permission_1= Manifest.permission.WRITE_EXTERNAL_STORAGE;
                    permission = ContextCompat.checkSelfPermission(context, permission_1);
                    permissionArr = new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE};
                    break;
                case REQUEST_ACCESS_LOCATION:
                    permissionDesc=context.getString(R.string.permissionLocation);
                    permission_1=Manifest.permission.ACCESS_FINE_LOCATION;
                    permission = ContextCompat.checkSelfPermission(context, permission_1);
                    permissionArr = new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION};
                    break;
                case REQUEST_ACCESS_CONFIG:
                case REQUEST_ACCESS_SYNC:
                    permissionDesc=context.getString(R.string.permissionCalendar) + "," +
                            context.getString(R.string.permissionContact);
                    permission_1 = Manifest.permission.WRITE_CALENDAR;
                    permission_2 = Manifest.permission.GET_ACCOUNTS;
                    permission = ContextCompat.checkSelfPermission(context, permission_1);
                    if (permission== PackageManager.PERMISSION_GRANTED) {
                        permission = ContextCompat.checkSelfPermission(context, permission_2);
                    }
                    permissionArr = new String[]{
                            Manifest.permission.WRITE_CALENDAR,
                            Manifest.permission.READ_CALENDAR,

                            Manifest.permission.GET_ACCOUNTS,
                            Manifest.permission.READ_CONTACTS};
                    break;
                case REQUEST_ACCESS_CALENDAR:
                default:
                    permissionDesc=context.getString(R.string.permissionCalendar);
                    permission_1 = Manifest.permission.WRITE_CALENDAR;
                    permission = ContextCompat.checkSelfPermission(context, permission_1);
                    permissionArr = new String[]{
                            Manifest.permission.WRITE_CALENDAR,
                            Manifest.permission.READ_CALENDAR};
                    break;
            }
            if (permission == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                if (suppressRequestPermission) return false;
                if (activity==null) return false;

                boolean firstAsking = true;//AxTools.getPrefBoolean("MyPermission" + permission_1, true);
                AxTools.setPrefBoolean("MyPermission" + permission_1, false);
                if (firstAsking) {
                    MyAlert myAlert = new MyAlert(activity, activity.getString(R.string.PermissionReason), new MyAlert.OkListener(){
                        @Override
                        public void ready(MyAlert alert) {
                            alert.dismiss();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                activity.requestPermissions(permissionArr, requestCode);//弹出对话框接收权限
                            }
                        }
                    });
                    myAlert.show();
                } else {
                    boolean showDialog = !activity.shouldShowRequestPermissionRationale(permission_1);
                    if (showDialog && (!TextUtils.isEmpty(permission_2))) {
                        showDialog = !activity.shouldShowRequestPermissionRationale(permission_2);
                    }
                    if (showDialog) {
                        MyAlert.show(activity, String.format(activity.getString(R.string.permissionRequest), activity.getString(R.string.BtnOk), permissionDesc), new MyAlert.OkListener() {
                            @SuppressLint("NewApi")
                            @Override
                            public void ready(MyAlert alert) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", activity.getPackageName(), null));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                activity.startActivity(intent);
                            }
                        });
                        return false;
                    }
                    activity.requestPermissions(permissionArr, requestCode);//弹出对话框接收权限
                }
                return false;
            }
        }
        return true;
    }
    public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults, Callback callback) {
        boolean allGranted=true;
        for (int i=0;i<grantResults.length;i++){
            if (grantResults[i]!=PackageManager.PERMISSION_GRANTED){
                allGranted=false;
                break;
            }
        }
        if (callback!=null) callback.onRequestResult(requestCode,allGranted);
        return allGranted;
    }
}
