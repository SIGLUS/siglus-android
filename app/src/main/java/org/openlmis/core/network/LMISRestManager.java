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

package org.openlmis.core.network;


import static javax.net.ssl.HttpsURLConnection.getDefaultHostnameVerifier;
import static org.openlmis.core.utils.Constants.BASIC_AUTH;
import static org.openlmis.core.utils.Constants.GRANT_TYPE;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import androidx.annotation.NonNull;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import lombok.SneakyThrows;
import org.openlmis.core.BuildConfig;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.NetWorkException;
import org.openlmis.core.exceptions.SyncServerException;
import org.openlmis.core.exceptions.UnauthorizedException;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.ProgramDataForm;
import org.openlmis.core.model.ReportTypeForm;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.Service;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.User;
import org.openlmis.core.network.adapter.ProductsResponseAdapter;
import org.openlmis.core.network.adapter.ProgramAdapter;
import org.openlmis.core.network.adapter.ProgramDataFormAdapter;
import org.openlmis.core.network.adapter.ReportTypeAdapter;
import org.openlmis.core.network.adapter.RnrFormAdapter;
import org.openlmis.core.network.adapter.ServiceAdapter;
import org.openlmis.core.network.adapter.StockCardAdapter;
import org.openlmis.core.network.model.DataErrorResponse;
import org.openlmis.core.network.model.SyncDownLatestProductsResponse;
import org.openlmis.core.network.model.UserResponse;
import org.openlmis.core.service.SyncService;
import retrofit.Callback;
import retrofit.ErrorHandler;
import retrofit.RequestInterceptor;
import retrofit.RequestInterceptor.RequestFacade;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Client;
import retrofit.client.OkClient;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;
import roboguice.RoboGuice;

public class LMISRestManager {

  private static LMISRestManager instance;
  private final LMISRestApi lmisRestApi;
  private final SyncService syncService;

  protected LMISRestManager(Context context) {
    String baseUrl = context.getString(R.string.server_base_url);

    RestAdapter.Builder restBuilder = new RestAdapter.Builder()
        .setEndpoint(baseUrl)
        .setErrorHandler(new APIErrorHandler())
        .setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.BASIC)
        .setRequestInterceptor(getRequestInterceptor())
        .setClient(getSSLClient())
        .setConverter(registerTypeAdapter());

    lmisRestApi = restBuilder.build().create(LMISRestApi.class);
    syncService = RoboGuice.getInjector(context).getInstance(SyncService.class);
    instance = this;
  }

  @NonNull
  protected Client getSSLClient() {
    OkHttpClient client = getOkHttpClient();

    try {
      SSLContext sslContext = SSLContext.getDefault();

      client.setSslSocketFactory(sslContext.getSocketFactory());
      //OVERRIDE HOSTNAME VERIFIER BECAUSE WE CONNECT TO ELB DIRECTLY DUE TO OCCASIONAL DNS ISSUES IN MOZAMBIQUE
      client.setHostnameVerifier((hostname, session) -> {

        X509Certificate cert;
        try {
          cert = (X509Certificate) session.getPeerCertificates()[0];
          if (cert.getSubjectDN().getName().equals("CN=lmis.cmam.gov.mz")) {
            return true;
          }
        } catch (SSLPeerUnverifiedException e) {
          new LMISException(e, "LMISRestManager,verify").reportToFabric();
        }

        return getDefaultHostnameVerifier().verify(hostname, session);
      });
    } catch (Exception e) {
      new LMISException(e, "LMISRestManager,ssl").reportToFabric();
    }
    return new OkClient(client);
  }

  public static LMISRestManager getInstance(Context context) {
    if (instance == null) {
      instance = new LMISRestManager(context);
    }
    return instance;
  }

  public LMISRestApi getLmisRestApi() {
    return lmisRestApi;
  }

  protected OkHttpClient getOkHttpClient() {
    OkHttpClient httpClient = new OkHttpClient();
    httpClient.setReadTimeout(20, TimeUnit.MINUTES);
    httpClient.setConnectTimeout(30, TimeUnit.SECONDS);
    httpClient.setWriteTimeout(60, TimeUnit.SECONDS);
    httpClient.interceptors();

    return httpClient;
  }

  @NonNull
  private RequestInterceptor getRequestInterceptor() {
    return request -> {
      User user = UserInfoMgr.getInstance().getUser();
      if (user != null && !user.getIsTokenExpired()) {
        request.addHeader("Authorization", user.getTokenType() + " " + user.getAccessToken());
        request.addHeader("UserName", user.getUsername());
        request.addHeader("FacilityCode", user.getFacilityCode());
        request.addHeader("FacilityName", user.getFacilityName());
      } else {
        request.addHeader("Authorization", BASIC_AUTH);
      }

      if (BuildConfig.MONITOR_DEVICE_ID) {
        request.addHeader("UniqueId", getAndroidId());
      }

      addDeviceInfoToRequestHeader(request);
    };
  }

  private void addDeviceInfoToRequestHeader(RequestFacade request) {
    String versionCode = String.valueOf(BuildConfig.VERSION_CODE);
    String deviceInfo = "OS: " + Build.VERSION.RELEASE
        + " Model: " + android.os.Build.BRAND + " " + android.os.Build.MODEL;
    request.addHeader("DeviceInfo", deviceInfo);
    request.addHeader("VersionCode", versionCode);
    request.addHeader("AndroidSDKVersion", Build.VERSION.SDK_INT + "");
  }

  private GsonConverter registerTypeAdapter() {
    return new GsonConverter(new GsonBuilder()
        .registerTypeAdapter(RnRForm.class, new RnrFormAdapter())
        .registerTypeAdapter(StockCard.class, new StockCardAdapter())
        .registerTypeAdapter(ProgramDataForm.class, new ProgramDataFormAdapter())
        .registerTypeAdapter(ReportTypeForm.class, new ReportTypeAdapter())
        .registerTypeAdapter(Service.class, new ServiceAdapter())
        .registerTypeAdapter(Program.class, new ProgramAdapter())
        .registerTypeAdapter(SyncDownLatestProductsResponse.class, new ProductsResponseAdapter())
        .create());
  }

  private String getAndroidId() {
    return Settings.Secure
        .getString(LMISApp.getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
  }

  class APIErrorHandler implements ErrorHandler {

    @Override
    public Throwable handleError(RetrofitError cause) {
      Response r = cause.getResponse();
      if (r != null && r.getStatus() == 401) {
        UserInfoMgr.getInstance().getUser().setIsTokenExpired(true);
        refreshUserAuthorize(UserInfoMgr.getInstance().getUser(), cause);
      }
      if (r != null && r.getStatus() == 400) {
        return new SyncServerException(
            ((DataErrorResponse) cause.getBodyAs(DataErrorResponse.class)).getError());
      }
      if (r != null && r.getStatus() == 500) {
        return new SyncServerException(LMISApp.getContext().getString(R.string.sync_server_error));
      }
      if (cause.getKind() == RetrofitError.Kind.NETWORK) {
        return new NetWorkException(cause);
      }
      return new LMISException(cause);
    }
  }

  public void refreshUserAuthorize(User user, RetrofitError cause) {
    LMISApp.getInstance().getRestApi()
        .authorizeUser(GRANT_TYPE, user.getUsername(), user.getPassword(),
            new Callback<UserResponse>() {
              @SneakyThrows
              @Override
              public void success(UserResponse userResponse, Response response) {
                if (userResponse == null || userResponse.getAccessToken() == null) {
                  throw new UnauthorizedException(cause);
                } else {
                  user.setAccessToken(userResponse.getAccessToken());
                  user.setTokenType(userResponse.getTokenType());
                  user.setIsTokenExpired(false);
                  UserInfoMgr.getInstance().setUser(user);
                  syncService.requestSyncImmediatelyByTask();
                }
              }

              @SneakyThrows
              @Override
              public void failure(RetrofitError error) {
                if (error.getCause() instanceof NetWorkException) {
                  throw new NetWorkException(cause);
                } else {
                  throw new UnauthorizedException(cause);
                }
              }
            });
  }

}
