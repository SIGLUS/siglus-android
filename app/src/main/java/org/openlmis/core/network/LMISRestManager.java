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


import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;

import com.google.gson.GsonBuilder;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.OkHttpClient;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.NetWorkException;
import org.openlmis.core.exceptions.SyncServerException;
import org.openlmis.core.exceptions.UnauthorizedException;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.ProgramDataForm;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.User;
import org.openlmis.core.network.adapter.ProductAdapter;
import org.openlmis.core.network.adapter.ProgramDataFormAdapter;
import org.openlmis.core.network.adapter.RnrFormAdapter;
import org.openlmis.core.network.adapter.StockCardAdapter;
import org.openlmis.core.network.model.DataErrorResponse;

import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

import retrofit.ErrorHandler;
import retrofit.RequestInterceptor;
import retrofit.RequestInterceptor.RequestFacade;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Client;
import retrofit.client.OkClient;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;

import static javax.net.ssl.HttpsURLConnection.getDefaultHostnameVerifier;

public class LMISRestManager {

    private static LMISRestManager instance;
    private LMISRestApi lmisRestApi;

    protected LMISRestManager(Context context) {
        String baseUrl = context.getString(R.string.server_base_url);

        RestAdapter.Builder restBuilder = new RestAdapter.Builder()
                .setEndpoint(baseUrl)
                .setErrorHandler(new APIErrorHandler())
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setRequestInterceptor(getRequestInterceptor())
                .setClient(getSSLClient())
                .setConverter(registerTypeAdapter());

        lmisRestApi = restBuilder.build().create(LMISRestApi.class);
        instance = this;
    }

    @NonNull
    protected Client getSSLClient() {
        OkHttpClient client = getOkHttpClient();

        try {
            SSLContext sslContext = SSLContext.getDefault();

            client.setSslSocketFactory(sslContext.getSocketFactory());
            client.setHostnameVerifier(new HostnameVerifier() {
                //OVERRIDE HOSTNAME VERIFIER BECAUSE WE CONNECT TO ELB DIRECTLY DUE TO OCCASIONAL DNS ISSUES IN MOZAMBIQUE
                @Override
                public boolean verify(String hostname, SSLSession session) {

                    X509Certificate cert;
                    try {
                        cert = (X509Certificate) session.getPeerCertificates()[0];
                        if (cert.getSubjectDN().getName().equals("CN=lmis.cmam.gov.mz")) {
                            return true;
                        }
                    } catch (SSLPeerUnverifiedException e) {
                        new LMISException(e).reportToFabric();
                    }

                    return getDefaultHostnameVerifier().verify(hostname, session);
                }
            });
        } catch (Exception e) {
            new LMISException(e).reportToFabric();
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
        httpClient.setReadTimeout(1, TimeUnit.MINUTES);
        httpClient.setConnectTimeout(15, TimeUnit.SECONDS);
        httpClient.setWriteTimeout(30, TimeUnit.SECONDS);

        return httpClient;
    }

    @NonNull
    private RequestInterceptor getRequestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                User user = UserInfoMgr.getInstance().getUser();
                if (user != null) {
                    String basic = Credentials.basic(user.getUsername(), user.getPassword());
                    request.addHeader("Authorization", basic);
                    request.addHeader("UserName", user.getUsername());
                    request.addHeader("FacilityName", user.getFacilityName());
                }
                addDeviceInfoToRequestHeader(request);
            }
        };
    }

    private void addDeviceInfoToRequestHeader(RequestFacade request) {
        String deviceInfo = "OS: " + Build.VERSION.RELEASE
                + " Model: " + android.os.Build.BRAND + " " + android.os.Build.MODEL;
        request.addHeader("DeviceInfo", deviceInfo);
    }

    private GsonConverter registerTypeAdapter() {
        return new GsonConverter(new GsonBuilder()
                .registerTypeAdapter(RnRForm.class, new RnrFormAdapter())
                .registerTypeAdapter(Product.class, new ProductAdapter())
                .registerTypeAdapter(StockCard.class, new StockCardAdapter())
                .registerTypeAdapter(ProgramDataForm.class, new ProgramDataFormAdapter())
                .create());
    }

    class APIErrorHandler implements ErrorHandler {
        @Override
        public Throwable handleError(RetrofitError cause) {
            Response r = cause.getResponse();
            if (r != null && r.getStatus() == 401) {
                return new UnauthorizedException(cause);
            }
            if (r != null && r.getStatus() == 400) {
                return new SyncServerException(((DataErrorResponse) cause.getBodyAs(DataErrorResponse.class)).getError());
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

}
