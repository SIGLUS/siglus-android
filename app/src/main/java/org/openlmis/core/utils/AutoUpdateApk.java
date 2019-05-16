
package org.openlmis.core.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings.Secure;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;
import org.openlmis.core.R;

public class AutoUpdateApk {
    MediaType MEDIA_TYPE = MediaType.parse("application/json");

    /**
     * This class is supposed to be instantiated in any of your activities or,
     * better yet, in Application subclass. Something along the lines of:
     * <p>
     * <pre>
     * private AutoUpdateApk aua;	<-- you need to add this line of code
     *
     * public void onCreate(Bundle savedInstanceState) {
     * 	super.onCreate(savedInstanceState);
     * 	setContentView(R.layout.main);
     *
     * 	aua = new AutoUpdateApk(getApplicationContext());	<-- and add this line too
     * </pre>
     *
     * @param ctx    parent activity context
     * @param apiURL server API path may be relative to server (eg. /myapi/updater)
     * or absolute, depending on server implementation : relative
     * path and server is mandatory if server's implementation
     * @param server server name and port (eg. myserver.domain.com:8123 ). Should
     * be null when using absolutes apiPath.
     */

    private static final String DOWNLOADED_LATEST_VERSIONCODE = "downloaded_latest_versioncode";

    public AutoUpdateApk(Context ctx, String apiPath, String server) {
        setupVariables(ctx);
        this.server = server;
        this.apiPath = apiPath;
    }

    // set name to display in notification popup (default = application label)
    //
    public static void setName(String name) {
        appName = name;
    }

    // call this if you want to perform update on demand
    // (checking for updates more often than once an hour is not recommended
    // and polling server every few minutes might be a reason for suspension)
    //
    public void checkUpdatesManually() {
        checkUpdates(true); // force update check
    }

    //
    // ---------- everything below this line is private and does not belong to
    // the public API ----------
    //
    protected final static String TAG = "AutoUpdateApk1";

    private final static String ANDROID_PACKAGE = "application/vnd.android.package-archive";

    protected final String server;
    protected final String apiPath;

    protected static Context context = null;
    protected static SharedPreferences preferences;
    private final static String LAST_UPDATE_KEY = "last_update";
    private static long last_update = 0;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder builder;
    private Notification notification;

    private static int versionCode = 0; // as low as it gets
    private static String packageName;
    private static String appName;
    private static int device_id;

    public static final long MINUTES = 60 * 1000;
    public static final long HOURS = 60 * MINUTES;
    public static final long DAYS = 24 * HOURS;

    // 3-4 hours in dev.mode, 1-2 days for stable releases
    private long updateInterval = 3 * HOURS; // how often to check

    private static boolean mobile_updates = false; // download updates over wifi
    // only

    private final static Handler updateHandler = new Handler();
    protected final static String UPDATE_FILE = "update_file";
    protected final static String SILENT_FAILED = "silent_failed";
    private final static String MD5_TIME = "md5_time";
    private final static String MD5_KEY = "md5";

    private static int NOTIFICATION_ID = 0xDEADBEEF;
    private static long WAKEUP_INTERVAL = 500;
    private PackageInfo packageInfo = null;

    private Runnable periodicUpdate = new Runnable() {
        @Override
        public void run() {
            checkUpdates(false);
            updateHandler.removeCallbacks(periodicUpdate); // remove whatever
            // others may have
            // posted
            updateHandler.postDelayed(this, WAKEUP_INTERVAL);
        }
    };

    private BroadcastReceiver connectivity_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            NetworkInfo currentNetworkInfo = (NetworkInfo) intent
                    .getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);

            // do application-specific task(s) based on the current network
            // state, such
            // as enabling queuing of HTTP requests when currentNetworkInfo is
            // connected etc.
            boolean not_mobile = currentNetworkInfo.getTypeName()
                    .equalsIgnoreCase("MOBILE") ? false : true;
            if (currentNetworkInfo.isConnected()
                    && (mobile_updates || not_mobile)) {
                checkUpdates(false);
                updateHandler.postDelayed(periodicUpdate, updateInterval);
            } else {
                updateHandler.removeCallbacks(periodicUpdate); // no network
                // anyway
            }
        }
    };

    private void setupVariables(Context ctx) {
        context = ctx;

        packageName = context.getPackageName();
        preferences = context.getSharedPreferences(packageName + "_" + TAG,
                Context.MODE_PRIVATE);
        device_id = crc32(Secure.getString(context.getContentResolver(), Secure.ANDROID_ID));
        last_update = preferences.getLong("last_update", 0);
        NOTIFICATION_ID += crc32(packageName);
        // schedule.add(new ScheduleEntry(0,24));

        ApplicationInfo appinfo = context.getApplicationInfo();
        if (appinfo.labelRes != 0) {
            appName = context.getString(appinfo.labelRes);
        } else {
            Log_w(TAG, "unable to find application label");
        }
        removeOldPackage(appinfo);
//        raise_notification();

        if (haveInternetPermissions()) {
            context.registerReceiver(connectivity_receiver, new IntentFilter(
                    ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    private void removeOldPackage(ApplicationInfo appinfo) {
        if (new File(appinfo.sourceDir).lastModified() > preferences.getLong(MD5_TIME, 0)) {
            preferences.edit().putString(MD5_KEY, MD5Hex(appinfo.sourceDir)).commit();
            preferences.edit().putLong(MD5_TIME, System.currentTimeMillis()).commit();

            String update_file = preferences.getString(UPDATE_FILE, "");
            if (update_file.length() > 0) {
                boolean isDeletedSuccessfule = new File(context.getFilesDir().getAbsolutePath() + "/" + update_file).delete();
                if (isDeletedSuccessfule) {
                    preferences.edit()
                            .remove(UPDATE_FILE)
                            .remove(SILENT_FAILED)
                            .remove(DOWNLOADED_LATEST_VERSIONCODE).commit();
                }
            }
        }
    }

    private class CheckUpdateTask extends AsyncTask<Void, Long, String[]> {
        private OkHttpClient okHttpClient = new OkHttpClient();
        private List<String> retrieved = new LinkedList<>();

        public CheckUpdateTask() {
        }

        protected String[] doInBackground(Void... v) {
            String[] resultGetApkInfo = null;

            okHttpClient.setConnectTimeout(30, TimeUnit.SECONDS);
            okHttpClient.setReadTimeout(50, TimeUnit.SECONDS);

            //Step 1 get download apk key
            JSONObject postdata = new JSONObject();
            try {
                postdata.put("pkgname", packageName);
                postdata.put("version", versionCode);
                postdata.put("md5", preferences.getString(MD5_KEY, "0"));
                postdata.put("id", String.format("%08x", device_id));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            RequestBody getApkLinkBody = RequestBody.create(MEDIA_TYPE, postdata.toString());
            Request getApkLinkRequest = new Request.Builder()
                    .url(server != null ? server + apiPath : apiPath)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .post(getApkLinkBody)
                    .build();
            Log.d(TAG,"getApkLinkBody:"+getApkLinkBody);
            Call getApkLinkCall = okHttpClient.newCall(getApkLinkRequest);
            try {
                Response response = getApkLinkCall.execute();
                String str=null;
                if (response.code() == 200) {
                    str = response.body().string();
                    resultGetApkInfo = str.split("\n");
                }
                Log.d(TAG,"first response:"+str);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //step 2 download the apk file ..
            Log.d(TAG,"first resultGetApkInfo="+resultGetApkInfo.length);
            if (resultGetApkInfo.length > 1 && resultGetApkInfo[0].equalsIgnoreCase("have update")) {
                if (!retrieved.contains(resultGetApkInfo[1])) {
                    synchronized (retrieved) {
                        if (!retrieved.contains(resultGetApkInfo[1])) {
                            retrieved.add(resultGetApkInfo[1]);
                            OkHttpClient httpClient = new OkHttpClient();
                            Call call = httpClient.newCall(new Request.Builder().url((server != null) ? server + resultGetApkInfo[1]
                                    : resultGetApkInfo[1]).get().build());
                            Log_d(TAG, "server + result[1]=" + (server + resultGetApkInfo[1]));
                            try {
                                initNotification();
                                Response response = call.execute();
                                if (response.code() == 200) {
                                    InputStream inputStream = null;
                                    String fname = resultGetApkInfo[1]
                                            .substring(resultGetApkInfo[1].lastIndexOf('/') + 1)
                                            + ".apk";
                                    try {
                                        inputStream = response.body().byteStream();
                                        FileOutputStream output = context
                                                .openFileOutput(fname,
                                                        Context.MODE_WORLD_READABLE);
                                        long downloaded = 0;
                                        long target = response.body().contentLength();
                                        publishProgress(0L, target);
                                        byte[] data = new byte[1024];
                                        int count = 0;

                                        while ((count = inputStream.read(data)) != -1) {
                                            downloaded += count;
                                            output.write(data, 0, count);
                                            publishProgress(downloaded, target);
                                        }
                                        output.flush();
                                        output.close();
                                        inputStream.close();
                                        resultGetApkInfo[1] = fname;
                                        String versionCodeFromServer = resultGetApkInfo[2];
                                        if (resultGetApkInfo.length > 2 && versionCodeFromServer != null) {
                                            updateVersionCode(versionCodeFromServer);
                                        }
                                        return resultGetApkInfo;
                                    } catch (IOException ignore) {
                                        Log.d(TAG, "ignore  = ", ignore);
                                    } finally {
                                        if (inputStream != null) {
                                            inputStream.close();
                                        }
                                    }
                                } else {
                                    Log.e(TAG, "response !=200 = " + response.code());
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                Log.e(TAG, "response download Apk Filed = " + e.getMessage());
                            } finally {
                                Log_d(TAG, "finally server + result[1]=" + (server + resultGetApkInfo[1]));
                                Log_d(TAG, "finally server + result[2]=" + resultGetApkInfo[2]);
                            }
                        }
                    }
                }
            }
            return null;
        }

        private void updateVersionCode(String versionFromServer) {
            try {
                versionCode = Integer.parseInt(versionFromServer);
                preferences.edit().putInt(DOWNLOADED_LATEST_VERSIONCODE, versionCode).commit();
            } catch (NumberFormatException nfe) {
                Log_e(TAG, "Invalide version code", nfe);
            }
        }

        @Override
        protected void onProgressUpdate(Long... values) {
            builder.setProgress(100,(int)((values[0]/values[1])*100),false);
            builder.setContentText("Download Progress: " +(int)((values[0]*1.0/values[1])*100) +"%");
            notification = builder.build();
            notificationManager.notify(NOTIFICATION_ID,notification);
        }

        protected void onPreExecute() {
            // show progress bar or something
            Log_v(TAG, "checking if there's update on the server");
        }
        protected void onPostExecute(String[] result) {
            // kill progress bar here
            if (result != null) {
                Log_v(TAG, " reply from update server, and saved");
                if (result[0].equalsIgnoreCase("have update")) {
                    preferences.edit().putString(UPDATE_FILE, result[1]).commit();

                    String update_file_path = context.getFilesDir()
                            .getAbsolutePath() + "/" + result[1];
                    preferences.edit().putString(MD5_KEY, MD5Hex(update_file_path)).commit();
                    preferences.edit().putLong(MD5_TIME, System.currentTimeMillis()).commit();
                }

                builder.setContentTitle("Download Completed")
                        .setContentText("Click to install")
                        .setProgress(0,0,false)
                        .setAutoCancel(true);
                String update_file = preferences.getString(UPDATE_FILE, "");
                Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
                notificationIntent.setDataAndType(
                        Uri.parse("file://"
                                + context.getFilesDir().getAbsolutePath() + "/"
                                + update_file), ANDROID_PACKAGE);
                PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                        notificationIntent, 0);
                notification = builder.setContentIntent(contentIntent).build();
                notificationManager.notify(NOTIFICATION_ID,notification);
            } else {
                Log_v(TAG, "no reply from update server");
            }
        }
    }

    private void checkUpdates(boolean forced) {
        long now = System.currentTimeMillis();
        if (forced || (last_update + updateInterval) < now) {
            if (!isDownloadedApkValid()) {
                versionCode = packageInfo.versionCode;
            }
            new CheckUpdateTask().execute();
            last_update = System.currentTimeMillis();
            preferences.edit().putLong(LAST_UPDATE_KEY, last_update).commit();


        }
    }

    private boolean isDownloadedApkValid() {
        String update_file = context.getFilesDir().getAbsolutePath() + "/"
                + preferences.getString(UPDATE_FILE, "");
        boolean isValid = false;
        if (update_file.length() > 0) {
            try {
                new JarFile(update_file); //Detect if the file have been corrupted
                isValid = true;
            } catch (Exception ex) {
                isValid = false;
            }
        }
        return isValid;
    }

    private void initNotification() {
        notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("siglus",
                    "My Notifications", NotificationManager.IMPORTANCE_DEFAULT);

            // Configure the notification channel.
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(appName + " update_available")
                .setContentText("Download progress : " +"0%")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(false)
                .setProgress(100,0,false);
        notification = builder.build();

    }

    private String MD5Hex(String filename) {
        final int BUFFER_SIZE = 8192;
        byte[] buf = new byte[BUFFER_SIZE];
        int length;
        try {
            FileInputStream fis = new FileInputStream(filename);
            BufferedInputStream bis = new BufferedInputStream(fis);
            MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            while ((length = bis.read(buf)) != -1) {
                md.update(buf, 0, length);
            }
            bis.close();

            byte[] array = md.digest();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100)
                        .substring(1, 3));
            }
            Log_v(TAG, "md5sum: " + sb.toString());
            return sb.toString();
        } catch (Exception e) {
            Log_e(TAG, e.getMessage());
        }
        return "md5bad";
    }

    private boolean haveInternetPermissions() {
        Set<String> required_perms = new HashSet<String>();
        required_perms.add("android.permission.INTERNET");
        required_perms.add("android.permission.ACCESS_WIFI_STATE");
        required_perms.add("android.permission.ACCESS_NETWORK_STATE");

        PackageManager pm = context.getPackageManager();
        String packageName = context.getPackageName();
        int flags = PackageManager.GET_PERMISSIONS;

        try {
            packageInfo = pm.getPackageInfo(packageName, flags);
            initVersionCode(packageInfo);
        } catch (NameNotFoundException e) {
            Log_e(TAG, e.getMessage());
        }

        if (packageInfo.requestedPermissions != null) {
            for (String p : packageInfo.requestedPermissions) {
                // Log_v(TAG, "permission: " + p.toString());
                required_perms.remove(p);
            }
            if (required_perms.size() == 0) {
                return true; // permissions are in order
            }
            // something is missing
            for (String p : required_perms) {
                Log_e(TAG, "required permission missing: " + p);
            }
        }
        Log_e(TAG,
                "INTERNET/WIFI access required, but no permissions are found in Manifest.xml");
        return false;
    }

    private void initVersionCode(PackageInfo packageInfo) {
        versionCode = preferences.getInt(DOWNLOADED_LATEST_VERSIONCODE, packageInfo.versionCode);
    }

    private static int crc32(String str) {
        byte bytes[] = str.getBytes();
        Checksum checksum = new CRC32();
        checksum.update(bytes, 0, bytes.length);
        return (int) checksum.getValue();
    }

    // logging facilities to enable easy overriding. thanks, Dan!
    //
    protected void Log_v(String tag, String message) {
        Log_v(tag, message, null);
    }

    protected void Log_v(String tag, String message, Throwable e) {
        log("v", tag, message, e);
    }

    protected void Log_d(String tag, String message) {
        Log_d(tag, message, null);
    }

    protected void Log_d(String tag, String message, Throwable e) {
        log("d", tag, message, e);
    }

    protected void Log_i(String tag, String message) {
        Log_d(tag, message, null);
    }

    protected void Log_i(String tag, String message, Throwable e) {
        log("i", tag, message, e);
    }

    protected void Log_w(String tag, String message) {
        Log_w(tag, message, null);
    }

    protected void Log_w(String tag, String message, Throwable e) {
        log("w", tag, message, e);
    }

    protected void Log_e(String tag, String message) {
        Log_e(tag, message, null);
    }

    protected void Log_e(String tag, String message, Throwable e) {
        log("e", tag, message, e);
    }

    protected void log(String level, String tag, String message, Throwable e) {
        if (message == null) {
            return;
        }
        if (level.equalsIgnoreCase("v")) {
            if (e == null)
                android.util.Log.v(tag, message);
            else
                android.util.Log.v(tag, message, e);
        } else if (level.equalsIgnoreCase("d")) {
            if (e == null)
                android.util.Log.d(tag, message);
            else
                android.util.Log.d(tag, message, e);
        } else if (level.equalsIgnoreCase("i")) {
            if (e == null)
                android.util.Log.i(tag, message);
            else
                android.util.Log.i(tag, message, e);
        } else if (level.equalsIgnoreCase("w")) {
            if (e == null)
                android.util.Log.w(tag, message);
            else
                android.util.Log.w(tag, message, e);
        } else {
            if (e == null)
                android.util.Log.e(tag, message);
            else
                android.util.Log.e(tag, message, e);
        }
    }

}
