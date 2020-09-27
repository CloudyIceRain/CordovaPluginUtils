package com.cordova.plugin.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PermissionHelper;
import org.json.JSONArray;
import org.json.JSONException;


public class pluginUtils extends CordovaPlugin {
    public CallbackContext m_permission_Callback = null;
    public boolean m_forcePermission = false;   ////强行获取所有权限，不给就不继续
    public void requestPermissions(String [] permissions, boolean forceGet, CallbackContext callback_ctx){
        m_forcePermission = forceGet;
        m_permission_Callback = callback_ctx;
        PermissionHelper.requestPermissions(this, 23534, permissions);
    }
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException{
        if (requestCode == 23534){
            for (int k =0; k< grantResults.length; k++){
                int r = grantResults[k];
                if (r == PackageManager.PERMISSION_DENIED){
                    ////被拒绝
                    if (m_forcePermission) {
                        //强行申请直到同意
                        PermissionHelper.requestPermission(this, 23534, permissions[k]);
                    }else{
                        //反馈告知申请失败
                        this.m_permission_Callback.error(permissions[k]);
                        return;
                    }
                }else{
                    //申请成功
                    this.m_permission_Callback.success(permissions[k]);
                }
            }
        }
    }
    public boolean checkPermission(String permission){
        return PermissionHelper.hasPermission(this, permission);
    }

    public static int getVersionCode(Context context){
        PackageManager pm = context.getPackageManager();//包管理器
        try {
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_CONFIGURATIONS);
            return packageInfo.versionCode;
        }catch(Exception e){
            e.printStackTrace();
            return -1;
        }
    }

    public static String getVersionName(Context context){
        PackageManager pm = context.getPackageManager();//包管理器
        try {
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_CONFIGURATIONS);
            return packageInfo.versionName;
        }catch(Exception e){
            e.printStackTrace();
            return "0.0.1";
        }
    }

    public static String getMetaDataByKey(Context context, String key) {
        if (context == null) {
            return null;
        }
        try {
            ApplicationInfo appInfo = null;
            appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            String msg = appInfo.metaData.getString(key);
            return msg;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        Activity activity = this.cordova.getActivity();
        Context context = this.cordova.getContext();

        if ("getVersionCode".equals(action)){
            ////获取版本code
            int vcode = getVersionCode(context);
            callbackContext.success(vcode);
            return true;
        }else if ("getVersionName".equals(action)){
            ////获取版本名称
            String vname = getVersionName(context);
            callbackContext.success(vname);
        	return true;
        }else if ("getMetaData".equals(action)){
            String key = args.getString(0);
            String vData = getMetaDataByKey(context, key);
            callbackContext.success(vData);
            return true;
        }else if ("requestPermissions".equals(action)){
            JSONArray json_per = args.getJSONArray(0);
            boolean forceget = args.getBoolean(1);
            String[] perStr = new String[]{};
            for (int i =0; i< json_per.length(); i++){
                perStr[i] = json_per.getString(i);
            }
            requestPermissions(perStr, forceget, callbackContext);
            return true;
        }else if ("checkPermission".equals(action)){
            String key = args.getString(0);
            boolean has = checkPermission(key);
            callbackContext.success(has?1:0);
            return true;
        }

        callbackContext.error(action + " is not a supported action");
        return false;
    }
}
