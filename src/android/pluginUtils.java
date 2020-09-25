package com.cordova.plugin.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONException;


public class pluginUtils extends CordovaPlugin {

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
        }

        callbackContext.error(action + " is not a supported action");
        return false;
    }
}
