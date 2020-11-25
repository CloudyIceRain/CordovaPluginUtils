package com.cordova.plugin.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PermissionHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;


public class pluginUtils extends CordovaPlugin {
    public CallbackContext m_permission_Callback = null;
    public boolean m_forcePermission = false;   ////强行获取所有权限，不给就不继续

    public void requestPermissions(String[] permissions, boolean forceGet, CallbackContext callback_ctx) {
        m_forcePermission = forceGet;
        m_permission_Callback = callback_ctx;
        PermissionHelper.requestPermissions(this, 23534, permissions);
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        if (requestCode == 23534) {
            for (int k = 0; k < grantResults.length; k++) {
                int r = grantResults[k];
                if (r == PackageManager.PERMISSION_DENIED) {
                    ////被拒绝
                    if (m_forcePermission) {
                        //强行申请直到同意
                        PermissionHelper.requestPermission(this, 23534, permissions[k]);
                    } else {
                        //反馈告知申请失败
                        this.m_permission_Callback.error(permissions[k]);
                        return;
                    }
                } else {
                    //申请成功
                    this.m_permission_Callback.success(permissions[k]);
                }
            }
        }
    }

    public boolean checkPermission(String permission) {
        return PermissionHelper.hasPermission(this, permission);
    }

    public static int getVersionCode(Context context) {
        PackageManager pm = context.getPackageManager();//包管理器
        try {
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_CONFIGURATIONS);
            return packageInfo.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static String getVersionName(Context context) {
        PackageManager pm = context.getPackageManager();//包管理器
        try {
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_CONFIGURATIONS);
            return packageInfo.versionName;
        } catch (Exception e) {
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

    /**
     * 通过WiFiManager获取mac地址
     * @param context
     * @return
     */
    private static String tryGetWifiMac(Context context) {
        WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wi = wm.getConnectionInfo();
        if (wi == null || wi.getMacAddress() == null) {
            return null;
        }
        if ("02:00:00:00:00:00".equals(wi.getMacAddress().trim())) {
            return null;
        } else {
            return wi.getMacAddress().trim();
        }
    }

    //获取当前手机网络的ip地址
    public static String getIPv4_mobile() {
        try {
            String ipv4;
            ArrayList<NetworkInterface> nilist = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface ni : nilist) {
                ArrayList<InetAddress> ialist = Collections.list(ni.getInetAddresses());
                for (InetAddress address : ialist) {
                    if (!address.isLoopbackAddress() && address instanceof Inet4Address) {
                        ipv4 = address.getHostAddress();
                        return ipv4;
                    }
                }

            }
        } catch (SocketException ex) {
            Log.e("getLocalIpAddress ex=", ex.toString());
        }
        return null;
    }

    public static String intToIp(int ipInt) {
        StringBuilder sb = new StringBuilder();
        sb.append(ipInt & 0xFF).append(".");
        sb.append((ipInt >> 8) & 0xFF).append(".");
        sb.append((ipInt >> 16) & 0xFF).append(".");
        sb.append((ipInt >> 24) & 0xFF);
        return sb.toString();
    }

    //仅仅获取wifi的ip地址
    public static String getIP_Wifi(Activity activity) {
        String ip = "";
        ConnectivityManager conMann = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = conMann.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetworkInfo.isConnected()) {
            WifiManager wifiManager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ipAddress = wifiInfo.getIpAddress();
            ip = intToIp(ipAddress);//获取wifi的ip地址
        }
        return ip;
    }

    //获取当前ip----优先wifi的ip地址，然后是手机网络的ip地址
    public static String getIpAddress(Activity activity) {
        String ip = "";
        ConnectivityManager conMann = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobileNetworkInfo = conMann.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifiNetworkInfo = conMann.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetworkInfo.isConnected()) {
            WifiManager wifiManager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ipAddress = wifiInfo.getIpAddress();
            ip = intToIp(ipAddress);//获取wifi的ip地址
        } else if (mobileNetworkInfo.isConnected()) {
            ip = getIPv4_mobile();////本地ipv4地址，依据移动网络
        }
        return ip;
    }

    ////MEID(Mobile Equipment Identifier)移动设备识别码，是CDMA手机的身份识别码，也是每台CDMA手机或通讯平板唯一的识别码，由14位数字组成。
    private static String getPhoneMEID(Activity activity, int slotIndex) {
        String meid = "";
        TelephonyManager tm = (TelephonyManager) activity.getSystemService(Service.TELEPHONY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            meid = tm.getMeid(slotIndex);
        }
        return meid;
    }

    ////IMEI(International Mobile Equipment Identity)是国际移动设备身份码的缩写，由15位数字组成的电子串号与每台手机一一对应，且该码全世界唯一。
    //slotIndex卡插槽id，早期版本传入会忽略这个id
    public static String getIMEI(Activity activity, int slotIndex) {
        String imei = "";
        try {
            TelephonyManager tm = (TelephonyManager) activity.getSystemService(Service.TELEPHONY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {               //O = 26 = android 8.0
                //8.0及之后版本
                imei = tm.getImei(slotIndex);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {         //M = 23 = android 6.0
                imei = tm.getDeviceId(slotIndex);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {   //LOLLIPOP = 21 = android 5.0
                //5.0及以后版本通过反射来获取imei号码
                //ObsoleteSdkInt---判断中的sdkint比最小sdk还低，可以忽略这个警告
                if (slotIndex > 0) return "";
                Method method = tm.getClass().getMethod("getImei");
                imei = (String) method.invoke(tm);
            } else {
                //更早的版本
                if (slotIndex > 0) return "";
                imei = tm.getDeviceId();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (imei == null) imei = "";
        return imei;
    }

    ////获取手机号码
    public static String getPhoneNum(Activity activity, int slotIndex) {
        String phoneNum = "";
        try {
            Context ctx = activity.getApplicationContext();
            TelephonyManager tm = (TelephonyManager) activity.getSystemService(Service.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(ctx, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(ctx, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                phoneNum = tm.getLine1Number();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return phoneNum;
    }

    public static String getDeviceInfoJson(Activity activity){
        //内存相关
        ActivityManager manager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        manager.getMemoryInfo(memoryInfo);

        //存储信息
        final StatFs statFs = new StatFs(Environment.getDataDirectory().getPath());
        long totalCounts = statFs.getBlockCountLong();//总共的block数
        long availableCounts = statFs.getAvailableBlocksLong() ; //获取可用的block数
        long size = statFs.getBlockSizeLong(); //每格所占的大小，一般是4KB==
        long availROMSize = availableCounts * size;//可用内部存储大小
        long totalROMSize = totalCounts *size; //内部存储总大小

        //显示信息
        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();

        //wifi的mac地址
        String wifi_mac = tryGetWifiMac(activity.getApplicationContext());
        //当前ip地址，默认是wifi的，没有wifi时是手机网络的
        String ip = getIpAddress(activity);

        JSONObject obj = new JSONObject();
        try {
            obj.put("device_version", Build.BRAND);
            obj.put("device_os", Build.USER);
            obj.put("device_baseOS", Build.VERSION.BASE_OS);
            obj.put("device_version", Build.VERSION.RELEASE);
            obj.put("sdkVersion", Build.VERSION.SDK_INT);
            obj.put("device_brand", Build.BRAND);               //设备厂商--Huawei Xiaomi等
            obj.put("device_manufacturer", Build.MANUFACTURER); //设备工厂名称（愿意是留给代工厂）实际上都是厂商，和brand基本一样
            obj.put("device_product", Build.PRODUCT);           //产品名称， 整个产品的名称：The name of the overall product.
            obj.put("device_model", Build.MODEL);               //产品名称， 版本即最终用户可见的名称：The end-user-visible name for the end product.
            obj.put("device_fingerprint", Build.FINGERPRINT);   //设备指纹
            obj.put("device_display", Build.DISPLAY);           //设备显示名称
            obj.put("productionDate", Build.TIME);              //出厂日期（如果刷机了，就是系统安装日期）

            obj.put("mac", wifi_mac);                           //获取mac地址
            obj.put("ip", ip);                                  //当前ip地址
            obj.put("ip_wifi", getIP_Wifi(activity));           //当前wifi的ip地址
            obj.put("ip_mobile", getIPv4_mobile());             //当前手机网络的ip地址

            //imei
            obj.put("imei", getIMEI(activity,0));       //卡槽1
            obj.put("imei1", getIMEI(activity,1));      //卡槽2
            //meid
            obj.put("meid", getPhoneMEID(activity, 0));   //卡槽1
            obj.put("meid1", getPhoneMEID(activity, 1));  //卡槽2

            obj.put("deviceWidth", displayMetrics.widthPixels);           //设备宽
            obj.put("deviceHeight", displayMetrics.heightPixels);         //设备高

            obj.put("ramCanUse",  memoryInfo.availMem);         //可用内存
            obj.put("ramTotal",  memoryInfo.totalMem);          //总内存
            obj.put("romCanUse",  availROMSize);         //可用存储
            obj.put("romTotal",  totalROMSize);          //总存储
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String ret = obj.toString();
        return ret;
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
        }else if ("getMetaDataByKey".equals(action)){
            String key = args.getString(0);
            String vData = getMetaDataByKey(context, key);
            callbackContext.success(vData);
            return true;
        }else if ("requestPermissions".equals(action)){
            JSONArray json_per = args.getJSONArray(0);
            boolean forceget = args.getBoolean(1);
            ArrayList<String> stringList = new ArrayList<String>();
            for (int i =0; i< json_per.length(); i++){
                stringList.add(json_per.getString(i));
            }
            String[] perStr = (String[])stringList.toArray(new String[stringList.size()]) ;
            requestPermissions(perStr, forceget, callbackContext);
            return true;
        }else if ("checkPermission".equals(action)){
            String key = args.getString(0);
            boolean has = checkPermission(key);
            callbackContext.success(has?1:0);
            return true;
        }else if ("getDeviceInfoJson".equals((action))){
            String ret = getDeviceInfoJson(activity);
            callbackContext.success(ret);
            return true;
        }

        callbackContext.error(action + " is not a supported action");
        return false;
    }
}
