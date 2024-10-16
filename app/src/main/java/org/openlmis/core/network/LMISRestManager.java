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

import static org.openlmis.core.utils.Constants.ANDROID_SDK_VERSION;
import static org.openlmis.core.utils.Constants.AUTHORIZATION;
import static org.openlmis.core.utils.Constants.BASIC_AUTH;
import static org.openlmis.core.utils.Constants.DEVICE_INFO;
import static org.openlmis.core.utils.Constants.FACILITY_CODE;
import static org.openlmis.core.utils.Constants.FACILITY_NAME;
import static org.openlmis.core.utils.Constants.GRANT_TYPE;
import static org.openlmis.core.utils.Constants.SIGLUS_API_ERROR_NOT_ANDROID;
import static org.openlmis.core.utils.Constants.SIGLUS_API_ERROR_NOT_REGISTERED_DEVICE;
import static org.openlmis.core.utils.Constants.UNIQUE_ID;
import static org.openlmis.core.utils.Constants.USER_NAME;
import static org.openlmis.core.utils.Constants.VERSION_CODE;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import androidx.annotation.NonNull;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;
import java.util.concurrent.TimeUnit;
import org.greenrobot.eventbus.EventBus;
import org.openlmis.core.BuildConfig;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.enumeration.LoginErrorType;
import org.openlmis.core.event.RefreshTokenFailedEvent;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.NetWorkException;
import org.openlmis.core.exceptions.SyncServerException;
import org.openlmis.core.exceptions.UnauthorizedException;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.Pod;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.ReportTypeForm;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.Service;
import org.openlmis.core.model.User;
import org.openlmis.core.model.repository.UserRepository;
import org.openlmis.core.network.adapter.PodAdapter;
import org.openlmis.core.network.adapter.ProductsResponseAdapter;
import org.openlmis.core.network.adapter.ProgramAdapter;
import org.openlmis.core.network.adapter.RegimenAdapter;
import org.openlmis.core.network.adapter.ReportTypeAdapter;
import org.openlmis.core.network.adapter.RnrFormAdapter;
import org.openlmis.core.network.adapter.RnrFormStatusEntryAdapter;
import org.openlmis.core.network.adapter.ServiceAdapter;
import org.openlmis.core.network.adapter.StockCardsResponseAdapter;
import org.openlmis.core.network.model.ErrorHandlingResponse;
import org.openlmis.core.network.model.RnrFormStatusEntry;
import org.openlmis.core.network.model.StockCardsLocalResponse;
import org.openlmis.core.network.model.SyncDownLatestProductsResponse;
import org.openlmis.core.network.model.SyncDownRegimensResponse;
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
  private final UserRepository userRepository;

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
    userRepository = RoboGuice.getInjector(context).getInstance(UserRepository.class);
  }

  @NonNull
  protected Client getSSLClient() {
    OkHttpClient client = getOkHttpClient();
    try {
      client.setSslSocketFactory(SSLFactory.getSocketFactory());
      client.setHostnameVerifier(SSLFactory.getHostnameVerifier());
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
        request.addHeader(AUTHORIZATION, user.getTokenType() + " " + user.getAccessToken());
        request.addHeader(USER_NAME, user.getUsername());
        request.addHeader(FACILITY_CODE, user.getFacilityCode());
        request.addHeader(FACILITY_NAME, user.getFacilityName());
      } else {
        request.addHeader(AUTHORIZATION, BASIC_AUTH);
      }
      request.addHeader(UNIQUE_ID, getAndroidId());
      addDeviceInfoToRequestHeader(request);
    };
  }

  private void addDeviceInfoToRequestHeader(RequestFacade request) {
    String versionCode = String.valueOf(BuildConfig.VERSION_CODE);
    String deviceInfo = "OS: " + Build.VERSION.RELEASE
        + " Model: " + android.os.Build.BRAND + " " + android.os.Build.MODEL;
    request.addHeader(DEVICE_INFO, deviceInfo);
    request.addHeader(VERSION_CODE, versionCode);
    request.addHeader(ANDROID_SDK_VERSION, Build.VERSION.SDK_INT + "");
  }

  private GsonConverter registerTypeAdapter() {
    return new GsonConverter(new GsonBuilder()
        .registerTypeAdapter(RnRForm.class, new RnrFormAdapter())
        .registerTypeAdapter(StockCardsLocalResponse.class, new StockCardsResponseAdapter())
        .registerTypeAdapter(ReportTypeForm.class, new ReportTypeAdapter())
        .registerTypeAdapter(Service.class, new ServiceAdapter())
        .registerTypeAdapter(Program.class, new ProgramAdapter())
        .registerTypeAdapter(SyncDownLatestProductsResponse.class, new ProductsResponseAdapter())
        .registerTypeAdapter(SyncDownRegimensResponse.class, new RegimenAdapter())
        .registerTypeAdapter(Pod.class, new PodAdapter())
        .registerTypeAdapter(RnrFormStatusEntry.class, new RnrFormStatusEntryAdapter())
        .create());
  }

  private String getAndroidId() {
    return Settings.Secure.getString(LMISApp.getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
  }

  class APIErrorHandler implements ErrorHandler {

    @Override
    public Throwable handleError(RetrofitError cause) {
      Response r = cause.getResponse();
      if (r != null) {
        switch (r.getStatus()) {
          case 400:
          case 404:
            ErrorHandlingResponse errorResponse1 = (ErrorHandlingResponse) cause.getBodyAs(ErrorHandlingResponse.class);
            if (errorResponse1.isAndroid()) {
              return new SyncServerException(errorResponse1.getMessageInEnglish(),
                  errorResponse1.getMessageInPortuguese());
            } else {
              return new SyncServerException();
            }
          case 401:
            UserInfoMgr.getInstance().getUser().setIsTokenExpired(true);
            refreshAccessToken(UserInfoMgr.getInstance().getUser(), cause);
            break;
          case 403:
            ErrorHandlingResponse errorResponse2 = (ErrorHandlingResponse) cause.getBodyAs(ErrorHandlingResponse.class);
            if (SIGLUS_API_ERROR_NOT_ANDROID.equals(errorResponse2.getMessageKey())) {
              return forbidNotAndroidUser();
            }
            if (SIGLUS_API_ERROR_NOT_REGISTERED_DEVICE.equals(errorResponse2.getMessageKey())) {
              return forbidNotSameDevice();
            }
            break;
          case 500:
            return new SyncServerException();
          default:
            break;
        }
      }
      if (cause.getKind() == RetrofitError.Kind.NETWORK) {
        return new NetWorkException(cause);
      }
      return new LMISException(cause);
    }

    private LMISException forbidNotAndroidUser() {
      EventBus.getDefault().post(LoginErrorType.NON_MOBILE_USER);
      userRepository.deleteLocalUser();
      return new LMISException(LMISApp.getContext().getResources().getString(R.string.msg_isAndroid_False));
    }

    private LMISException forbidNotSameDevice() {
      EventBus.getDefault().post(SIGLUS_API_ERROR_NOT_REGISTERED_DEVICE);
      return new LMISException(LMISApp.getContext().getResources().getString(R.string.msg_is_same_device_false));
    }
  }

  public void refreshAccessToken(User user, RetrofitError cause) {
    LMISApp.getInstance().getRestApi()
        .login(GRANT_TYPE, user.getUsername(), user.getPassword(), new Callback<UserResponse>() {

          @Override
          public void success(UserResponse userResponse, Response response) {
            if (userResponse == null || userResponse.getAccessToken() == null) {
              EventBus.getDefault().post(new RefreshTokenFailedEvent(new UnauthorizedException(cause)));
            } else {
              user.setAccessToken(userResponse.getAccessToken());
              user.setTokenType(userResponse.getTokenType());
              user.setIsTokenExpired(false);
              UserInfoMgr.getInstance().setUser(user);
              syncService.requestSyncImmediatelyByTask();
            }
          }

          @Override
          public void failure(RetrofitError error) {
            if (error.getCause() instanceof NetWorkException) {
              EventBus.getDefault().post(new RefreshTokenFailedEvent(new NetWorkException(cause)));
            } else {
              EventBus.getDefault().post(new RefreshTokenFailedEvent(new UnauthorizedException(cause)));
            }
          }
        });
  }

}
