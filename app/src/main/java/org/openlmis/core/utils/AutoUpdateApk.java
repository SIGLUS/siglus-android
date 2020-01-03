
package org.openlmis.core.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Settings.Secure;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.manager.SharedPreferenceMgr;

public class AutoUpdateApk {
    MediaType MEDIA_TYPE = MediaType.parse("application/json");

    public AutoUpdateApk(Context ctx, String apiPath, String server) {
        setupVariables(ctx);
        this.server = server;
        this.apiPath = apiPath;
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
    private final static String TAG = "AutoUpdateApk1";

    private final static String ANDROID_PACKAGE = "application/vnd.android.package-archive";

    private final String server;
    private final String apiPath;

    private Context context = null;
    private SharedPreferenceMgr preferences;
    private static long last_update = 0;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationB;

    private static int versionCode = 0; // as low as it gets
    private static String packageName;
    private static int device_id;

    private static final long MINUTES = 60 * 1000;
    private static final long HOURS = 60 * MINUTES;

    // 3-4 hours in dev.mode, 1-2 days for stable releases
    private long updateInterval = 3 * HOURS; // how often to check
    private static int NOTIFICATION_ID = 12;

    private void setupVariables(Context ctx) {
        context = ctx;

        packageName = context.getPackageName();
        preferences = SharedPreferenceMgr.getInstance();
        device_id = crc32(Secure.getString(context.getContentResolver(), Secure.ANDROID_ID));
        last_update = preferences.getLastUpdate();
        NOTIFICATION_ID += crc32(packageName);

        ApplicationInfo appinfo = context.getApplicationInfo();
        removeOldPackage(appinfo);
    }

    private void removeOldPackage(ApplicationInfo appinfo) {
        if (new File(appinfo.sourceDir).lastModified() > preferences.getMd5Time()) {
            preferences.setMd5Key(MD5Hex(appinfo.sourceDir));
            preferences.setMd5Time(LMISApp.getInstance().getCurrentTimeMillis());

            String updateFile = preferences.getUpdateFile();
            if (updateFile.length() > 0) {
                boolean isDeletedSuccessfule = new File(context.getFilesDir().getAbsolutePath() + "/" + updateFile).delete();
                if (isDeletedSuccessfule) {
                    preferences.removeDownloadInfo();
                }
            }
        }
    }

    private class CheckUpdateTask extends AsyncTask<Void, Long, String[]> {
        private OkHttpClient okHttpClient = new OkHttpClient();
        private List<String> retrieved = new LinkedList<>();

        protected String[] doInBackground(Void... v) {
            String[] resultGetApkInfo = null;

            okHttpClient.setConnectTimeout(30, TimeUnit.SECONDS);
            okHttpClient.setReadTimeout(50, TimeUnit.SECONDS);

            //Step 1 get download apk key
            JSONObject postdata = new JSONObject();
            try {
                postdata.put("pkgname", packageName);
                postdata.put("version", versionCode);
                postdata.put("md5", preferences.getMd5Key());
                postdata.put("id", String.format("%08x", device_id));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            RequestBody getApkLinkBody = RequestBody.create(MEDIA_TYPE, postdata.toString());
            Request getApkLinkRequest = new Request.Builder()
                    .url(server != null ? server + apiPath : apiPath)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .post(getApkLinkBody)
                    .build();
            Log.d(TAG, "getApkLinkBody:" + getApkLinkBody);
            Call getApkLinkCall = okHttpClient.newCall(getApkLinkRequest);
            try {
                Response response = getApkLinkCall.execute();
                String str = null;
                if (response.code() == 200) {
                    str = response.body().string();
                    resultGetApkInfo = str.split("\n");
                }
                Log.d(TAG, "first response:" + str);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (resultGetApkInfo == null) {
                return null;
            }
            //step 2 download the apk file ..
            return downloadApk(resultGetApkInfo);
        }


        private boolean isValidResponse(String[] info) {
            return info.length > 1 && info[0].equalsIgnoreCase("have update") && !retrieved.contains(info[1]);
        }

        private String[] processResponse(Response response, String[] resultGetApkInfo) throws IOException {
            if (response.code() == 200) {
                InputStream inputStream = null;
                String fname = resultGetApkInfo[1]
                        .substring(resultGetApkInfo[1].lastIndexOf('/') + 1)
                        + ".apk";
                try {
                    inputStream = response.body().byteStream();
                    FileOutputStream output = context
                            .openFileOutput(fname,
                                    Context.MODE_PRIVATE);
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
                    if (versionCodeFromServer != null) {
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
            return null;
        }

        private String[] downloadApk(String[] resultGetApkInfo) {
            if (isValidResponse(resultGetApkInfo)) {
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
                            return processResponse(response, resultGetApkInfo);
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
            return null;
        }

        private void updateVersionCode(String versionFromServer) {
            try {
                versionCode = Integer.parseInt(versionFromServer);
                preferences.setLastestVersionCode(versionCode);
            } catch (NumberFormatException nfe) {
                Log_e(TAG, "Invalide version code", nfe);
            }
        }

        @Override
        protected void onProgressUpdate(Long... values) {
            notificationB.setProgress(100, (int) ((values[0] * 1.0 / values[1]) * 100), false);
            notificationB.setContentText("Download Progress: " + (int) ((values[0] * 1.0 / values[1]) * 100) + "%");
            notificationManager.notify(NOTIFICATION_ID, notificationB.build());
        }

        @Override
        protected void onPreExecute() {
            // show progress bar or something
            Log_v(TAG, "checking if there's update on the server");
        }

        @Override
        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);
            if (notificationB == null) {
                return;
            }
            if (result != null) {
                notificationManager.cancel(NOTIFICATION_ID);

                String updateFile = preferences.getUpdateFile();
                Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
                notificationIntent.setDataAndType(
                        Uri.parse("file://" + context.getFilesDir().getAbsolutePath() + "/" + updateFile), ANDROID_PACKAGE);
                PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                        notificationIntent, 0);
                notificationB.setContentTitle(getString(R.string.upgrade_download_complete))
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentText(getString(R.string.upgrade_download_click_install))
                        .setPriority(NotificationCompat.DEFAULT_ALL)
                        .setProgress(0, 0, false)
                        .setOngoing(false)
                        .setAutoCancel(true)
                        .setContentIntent(contentIntent);
                Notification notification = notificationB.build();

                if (result[0].equalsIgnoreCase("have update")) {
                    preferences.setUpdateFile(result[1]);
                    String updateFilePath = context.getFilesDir()
                            .getAbsolutePath() + "/" + result[1];

                    preferences.setMd5Key(MD5Hex(updateFilePath));
                    preferences.setMd5Time(LMISApp.getInstance().getCurrentTimeMillis());
                }
                notificationManager.notify(NOTIFICATION_ID, notification);
            } else {
                notificationB.setContentTitle(getString(R.string.upgrade_download_Failed))
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentText(getString(R.string.upgrade_download_try))
                        .setPriority(NotificationCompat.DEFAULT_ALL)
                        .setContentIntent(null)
                        .setProgress(0, 0, false)
                        .setOngoing(false)
                        .setAutoCancel(true);
                notificationManager.notify(NOTIFICATION_ID, notificationB.build());
            }
        }
    }


    private String getString(int id) {
        return context.getString(id);
    }

    private void checkUpdates(boolean forced) {
        long now = LMISApp.getInstance().getCurrentTimeMillis();
        if (forced || (last_update + updateInterval) < now) {
            try {
                PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                versionCode = packageInfo.versionCode;
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
            new CheckUpdateTask().execute();
            last_update = LMISApp.getInstance().getCurrentTimeMillis();
            preferences.setLastUpdate(last_update);
        }
    }

    private void initNotification() {
        notificationB = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.upgrade_download_title))
                .setContentText(getString(R.string.upgrade_download_msg))
                .setPriority(NotificationCompat.DEFAULT_ALL)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setProgress(100, 0, true);

        if (notificationManager == null) {
            notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        } else {
            notificationManager.notify(NOTIFICATION_ID, notificationB.build());
        }
    }

    private String MD5Hex(String filename) {
        final int size = 8192;
        byte[] buf = new byte[size];
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

    private static int crc32(String str) {
        byte[] bytes = str.getBytes();
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
