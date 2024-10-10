/*
 * This program is part of the OpenLMIS logistics management information
 * system platform software.
 *
 * Copyright © 2015 ThoughtWorks, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should
 * have received a copy of the GNU Affero General Public License along with
 * this program. If not, see http://www.gnu.org/licenses. For additional
 * information contact info@OpenLMIS.org
 */

package org.openlmis.core.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.provider.Settings.Secure;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.network.SSLFactory;

public class AutoUpdateApk {

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
    checkUpdates(); // force update check
  }

  //
  // ---------- everything below this line is private and does not belong to
  // the public API ----------
  //
  private static final String TAG = "AutoUpdateApk1";
  private static final String ANDROID_PACKAGE = "application/vnd.android.package-archive";
  MediaType mediaType = MediaType.parse("application/json");


  private final String server;
  private final String apiPath;

  private Context context = null;
  private SharedPreferenceMgr preferences;
  private long lastUpdate = 0;
  private NotificationManager notificationManager;
  private NotificationCompat.Builder mNotificationOAndHigherBuilder;
  private NotificationCompat.Builder mNotificationBetweenLOLLIPOPAndroidOBuilder;
  private NotificationCompat.Builder mNotificationBetweenLOLLIPOPAndroidJELLYBEANBuilder;

  private int versionCode = 0; // as low as it gets
  private String packageName;
  private int deviceId;
  private int facilityCode;
  private String mDownloadApkDirectory;

  private int notificationId = 12;
  private static final String CHANNEL_ID = "upgrade_channel_id";
  private static final String CHANNEL_NAME = "upgrade_channel_name";
  private static final String CHANNEL_DESCRIPTION = "default channel";
  private PendingIntent mPendingIntent;

  private void setupVariables(Context ctx) {
    context = ctx;

    packageName = context.getPackageName();
    preferences = SharedPreferenceMgr.getInstance();
    deviceId = crc32(Secure.getString(context.getContentResolver(), Secure.ANDROID_ID));
    facilityCode = crc32(SharedPreferenceMgr.getInstance().getUserFacilityCode());
    lastUpdate = preferences.getLastUpdate();
    notificationId += crc32(packageName);
    mDownloadApkDirectory = context.getFilesDir().getAbsolutePath();

    ApplicationInfo appInfo = context.getApplicationInfo();
    removeOldPackage(appInfo);
  }

  private void removeOldPackage(ApplicationInfo appInfo) {
    if (new File(appInfo.sourceDir).lastModified() > preferences.getMd5Time()) {
      preferences.setMd5Key(md5Hex(appInfo.sourceDir));
      preferences.setMd5Time(LMISApp.getInstance().getCurrentTimeMillis());

      String updateFile = preferences.getUpdateFile();
      if (updateFile.length() > 0) {
        boolean isDeletedSuccess = new File(mDownloadApkDirectory + File.separator + updateFile).delete();
        if (isDeletedSuccess) {
          preferences.removeDownloadInfo();
        }
      }
    }
  }

  private class CheckUpdateTask extends AsyncTask<Void, Long, String[]> {

    private final OkHttpClient okHttpClient = new OkHttpClient();
    private final List<String> retrieved = new LinkedList<>();

    public CheckUpdateTask() {
      try {
        okHttpClient.setSslSocketFactory(SSLFactory.getSocketFactory());
        okHttpClient.setHostnameVerifier(SSLFactory.getHostnameVerifier());
      } catch (Exception e) {
        new LMISException(e, "LMISRestManager,ssl").reportToFabric();
      }
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
        postdata.put("md5", preferences.getMd5Key());
        postdata.put("id", String.format("%08x", deviceId));
        postdata.put("facilityCode", String.format("%08x", facilityCode));
      } catch (JSONException e) {
        Log.w(TAG, e);
      }
      RequestBody getApkLinkBody = RequestBody.create(mediaType, postdata.toString());
      Request getApkLinkRequest = new Request.Builder()
          .url(server != null ? server + apiPath : apiPath)
          .header("Content-Type", "application/x-www-form-urlencoded")
          .post(getApkLinkBody)
          .build();
      Log.i(TAG, "getApkLinkBody:" + getApkLinkBody);
      Call getApkLinkCall = okHttpClient.newCall(getApkLinkRequest);
      try {
        Response response = getApkLinkCall.execute();
        String str = null;
        if (response.code() == 200) {
          str = response.body().string();
          resultGetApkInfo = str.split("\n");
        }
        Log.i(TAG, "first response:" + str);
      } catch (IOException e) {
        Log.w(TAG, e);
      }
      if (resultGetApkInfo == null) {
        return ArrayUtils.EMPTY_STRING_ARRAY;
      }
      //step 2 download the apk file ..
      return doDownloadApk(resultGetApkInfo);
    }

    private String[] doDownloadApk(String[] resultGetApkInfo) {
      if (isValidResponse(resultGetApkInfo)) {
        synchronized (retrieved) {
          if (!retrieved.contains(resultGetApkInfo[1])) {
            retrieved.add(resultGetApkInfo[1]);
            OkHttpClient httpClient = new OkHttpClient();
            Call call = httpClient
                .newCall(new Request.Builder().url((server != null) ? server + resultGetApkInfo[1]
                    : resultGetApkInfo[1]).get().build());
            Log.i(TAG, "server + result[1]=" + (server + resultGetApkInfo[1]));
            try {
              showInitialNotification();
              Response response = call.execute();
              return writeToStorage(response, resultGetApkInfo);
            } catch (IOException e) {
              Log.w(TAG, e);
              Log.e(TAG, "response download Apk Filed = " + e.getMessage());
            } finally {
              Log.i(TAG, "finally server + result[1]=" + (server + resultGetApkInfo[1]));
              Log.i(TAG, "finally server + result[2]=" + resultGetApkInfo[2]);
            }
          }
        }
      }
      return ArrayUtils.EMPTY_STRING_ARRAY;
    }

    private boolean isValidResponse(String[] info) {
      return info.length > 1 && info[0].equalsIgnoreCase("have update") && !retrieved
          .contains(info[1]);
    }

    private FileOutputStream getOutputStream(String fileName) throws IOException {
      return context.openFileOutput(fileName,
          isNOrHigher() ? Context.MODE_PRIVATE : Context.MODE_WORLD_READABLE);
    }

    private boolean isNOrHigher() {
      return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }

    private String[] writeToStorage(Response response, String[] resultGetApkInfo)
        throws IOException {
      if (response.code() == 200) {
        InputStream inputStream = null;
        String fname = resultGetApkInfo[1]
            .substring(resultGetApkInfo[1].lastIndexOf('/') + 1)
            + ".apk";
        try {
          inputStream = response.body().byteStream();
          FileOutputStream output = getOutputStream(fname);
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
          Log.e(TAG, "ignore  = ", ignore);
        } finally {
          if (inputStream != null) {
            inputStream.close();
          }
        }
      } else {
        Log.e(TAG, "response !=200 = " + response.code());
      }
      return ArrayUtils.EMPTY_STRING_ARRAY;
    }

    private void updateVersionCode(String versionFromServer) {
      try {
        versionCode = Integer.parseInt(versionFromServer);
        preferences.setLastestVersionCode(versionCode);
      } catch (NumberFormatException nfe) {
        Log.e(TAG, "Invalide version code", nfe);
      }
    }

    @Override
    protected void onProgressUpdate(Long... values) {
      int maxAmount = 100;
      int progressAmount = (int) ((values[0] * 1.0 / values[1]) * maxAmount);
      if (isBetweenLollipopAndO()) {
        mNotificationBetweenLOLLIPOPAndroidOBuilder.setProgress(maxAmount, progressAmount, false);
        mNotificationBetweenLOLLIPOPAndroidOBuilder
            .setContentText(getString(R.string.upgrade_download_msg, progressAmount));
        notificationManager
            .notify(notificationId, mNotificationBetweenLOLLIPOPAndroidOBuilder.build());
      } else if (isLowerThanLollipop()) {
        mNotificationBetweenLOLLIPOPAndroidJELLYBEANBuilder
            .setProgress(maxAmount, progressAmount, false);
        mNotificationBetweenLOLLIPOPAndroidJELLYBEANBuilder
            .setContentText(getString(R.string.upgrade_download_msg, progressAmount));
        notificationManager
            .notify(notificationId, mNotificationBetweenLOLLIPOPAndroidJELLYBEANBuilder.build());
      } else if (isOAndHigher()) {
        mNotificationOAndHigherBuilder.setProgress(maxAmount, progressAmount, false);
        mNotificationOAndHigherBuilder
            .setContentText(getString(R.string.upgrade_download_msg, progressAmount));
        notificationManager.notify(notificationId, mNotificationOAndHigherBuilder.build());
      }
    }

    @Override
    protected void onPreExecute() {
      // show progress bar or something
      Log.v(TAG, "checking if there's update on the server");
    }

    @Override
    protected void onPostExecute(String[] result) {
      super.onPostExecute(result);
      if (isNotificationBuilderNull()) {
        Log.e(TAG, "isNotificationBuilderNull");
        return;
      }
      if (result != null) {
        if (result[0].equalsIgnoreCase("have update")) {
          preferences.setUpdateFile(result[1]);
          String updateFilePath = mDownloadApkDirectory + File.separator + result[1];

          Log.e(TAG, "updateFilePath  ==  " + updateFilePath);
          preferences.setMd5Key(md5Hex(updateFilePath));
          preferences.setMd5Time(LMISApp.getInstance().getCurrentTimeMillis());
        }
        String updateFile = preferences.getUpdateFile();
        Log.e(TAG, "updateFile= " + updateFile);

        Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
        Uri intentUri;
        if (isNOrHigher()) {
          File updateApk = new File(mDownloadApkDirectory + File.separator + updateFile);
          intentUri = FileProvider.getUriForFile(context,
              context.getApplicationInfo().packageName + ".FileProvider",
              updateApk);
        } else {
          intentUri = Uri.parse("file://" + mDownloadApkDirectory + File.separator + updateFile);
        }
        notificationIntent.setDataAndType(intentUri, ANDROID_PACKAGE)
            .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        mPendingIntent = PendingIntent.getActivity(context, 0,
            notificationIntent, 0);
        Log.e(TAG, " to completed..");
        updateNotificationToCompeted();

      }
    }

    private boolean isNotificationBuilderNull() {
      if (isBetweenLollipopAndO()) {
        return mNotificationBetweenLOLLIPOPAndroidOBuilder == null;
      } else if (isLowerThanLollipop()) {
        return mNotificationBetweenLOLLIPOPAndroidJELLYBEANBuilder == null;
      } else if (isOAndHigher()) {
        return mNotificationOAndHigherBuilder == null;
      }
      return true;
    }

    private void updateNotificationToCompeted() {
      if (isBetweenLollipopAndO()) {
        mNotificationBetweenLOLLIPOPAndroidOBuilder.setContentIntent(mPendingIntent)
            .setContentText(getString(R.string.upgrade_download_click_install))
            .setContentTitle(getString(R.string.upgrade_download_complete));
        notificationManager
            .notify(notificationId, mNotificationBetweenLOLLIPOPAndroidOBuilder.build());
      } else if (isLowerThanLollipop()) {
        mNotificationBetweenLOLLIPOPAndroidJELLYBEANBuilder.setContentIntent(mPendingIntent)
            .setContentText(getString(R.string.upgrade_download_click_install))
            .setContentTitle(getString(R.string.upgrade_download_complete));
        notificationManager
            .notify(notificationId, mNotificationBetweenLOLLIPOPAndroidJELLYBEANBuilder.build());
      } else if (isOAndHigher()) {
        mNotificationOAndHigherBuilder.setContentIntent(mPendingIntent)
            .setContentText(getString(R.string.upgrade_download_click_install))
            .setContentTitle(getString(R.string.upgrade_download_complete));
        notificationManager.notify(notificationId, mNotificationOAndHigherBuilder.build());
      }
    }

    private String getString(int resId, Object... formatArgs) {
      return context.getResources().getString(resId, formatArgs);
    }
  }

  private String getString(int id) {
    return context.getString(id);
  }

  private void checkUpdates() {
    try {
      PackageInfo packageInfo = context.getPackageManager()
          .getPackageInfo(context.getPackageName(), 0);
      versionCode =
          (VERSION.SDK_INT < VERSION_CODES.P) ? packageInfo.versionCode : (int) packageInfo.getLongVersionCode();
    } catch (Exception e) {
      Log.w(TAG, e);
    }
    new CheckUpdateTask().execute();
    lastUpdate = LMISApp.getInstance().getCurrentTimeMillis();
    preferences.setLastUpdate(lastUpdate);
  }

  private boolean isBetweenLollipopAndO() {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.O
        && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1;
  }

  private boolean isLowerThanLollipop() {
    return Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1;
  }

  private boolean isOAndHigher() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
  }

  public void showInitialNotification() {
    notificationManager = (NotificationManager) context
        .getSystemService(Context.NOTIFICATION_SERVICE);

    if (isBetweenLollipopAndO()) {
      mNotificationBetweenLOLLIPOPAndroidOBuilder = initNotificationBuilderForLowerO();
    } else if (isLowerThanLollipop()) {
      mNotificationBetweenLOLLIPOPAndroidJELLYBEANBuilder = initNotificationBuilderForLowerO();
    } else if (isOAndHigher()) {
      NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID,
          CHANNEL_NAME,
          NotificationManager.IMPORTANCE_LOW);
      mChannel.setDescription(CHANNEL_DESCRIPTION);
      mChannel.enableLights(true);
      mChannel.enableVibration(true);
      mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
      mChannel.setShowBadge(false);
      notificationManager.createNotificationChannel(mChannel);

      mNotificationOAndHigherBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
          .setContentTitle(getString(R.string.upgrade_download_title))
          .setSmallIcon(R.mipmap.ic_launcher)
          .setContentText(getString(R.string.upgrade_download_click_install))
          .setPriority(NotificationCompat.DEFAULT_ALL)
          .setProgress(0, 0, false)
          .setOngoing(false)
          .setAutoCancel(true);
    }

  }

  private NotificationCompat.Builder initNotificationBuilderForLowerO() {
    return new NotificationCompat.Builder(context)
        .setContentTitle(getString(R.string.upgrade_download_title))
        .setSmallIcon(R.mipmap.ic_launcher)
        .setPriority(NotificationCompat.DEFAULT_ALL)
        .setProgress(0, 0, false)
        .setOngoing(false)
        .setAutoCancel(true);
  }

  private String md5Hex(String filename) {
    final int size = 8192;
    byte[] buf = new byte[size];
    int length;
    try (FileInputStream fis = new FileInputStream(
        filename); BufferedInputStream bis = new BufferedInputStream(fis)) {
      MessageDigest md = MessageDigest.getInstance("MD5");
      while ((length = bis.read(buf)) != -1) {
        md.update(buf, 0, length);
      }
      byte[] array = md.digest();
      StringBuilder sb = new StringBuilder();
      for (byte b : array) {
        sb.append(Integer.toHexString((b & 0xFF) | 0x100)
            .substring(1, 3));
      }
      Log.v(TAG, "md5sum: " + sb);
      return sb.toString();
    } catch (Exception e) {
      Log.v(TAG, e.getMessage());
    }
    return "md5bad";
  }

  private static int crc32(String str) {
    byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
    Checksum checksum = new CRC32();
    checksum.update(bytes, 0, bytes.length);
    return (int) checksum.getValue();
  }

}
