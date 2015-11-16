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


import android.support.annotation.NonNull;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonSerializer;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.OkHttpClient;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.UnauthorizedException;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.User;
import org.openlmis.core.network.adapter.ProductsAdapter;
import org.openlmis.core.network.adapter.RnrFormAdapter;
import org.openlmis.core.network.adapter.RnrFormAdapterForFeatureToggle;

import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

import lombok.Data;
import retrofit.ErrorHandler;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;

public class LMISRestManager {

    public String END_POINT;

    protected RestAdapter restAdapter;
    protected LMISRestApi lmisRestApi;
    private String hostName;

    public LMISRestManager() {
        END_POINT = LMISApp.getContext().getResources().getString(R.string.server_base_url);
        try {
            hostName = new URL(END_POINT).getHost();
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestInterceptor requestInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                User user = UserInfoMgr.getInstance().getUser();
                if (user != null) {
                    String basic = Credentials.basic(user.getUsername(), user.getPassword());
                    request.addHeader("Authorization", basic);
                }
            }
        };

        restAdapter = new RestAdapter.Builder()
                .setEndpoint(END_POINT)
                .setErrorHandler(new MyErrorHandler())
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setRequestInterceptor(requestInterceptor)
                .setConverter(registerTypeAdapter())
                .setClient(new OkClient(getSSLClient()))
                .build();

        lmisRestApi = restAdapter.create(LMISRestApi.class);
    }

    public OkHttpClient getSSLClient() {
        OkHttpClient client = new OkHttpClient();

        //set timeout to 1 minutes
        client.setReadTimeout(1, TimeUnit.MINUTES);
        client.setConnectTimeout(15, TimeUnit.SECONDS);
        client.setWriteTimeout(30, TimeUnit.SECONDS);

        // loading CAs from an InputStream
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream cert = LMISApp.getContext().getResources().openRawResource(R.raw.ssl_cert);
            Certificate ca = null;
            try {
                ca = cf.generateCertificate(cert);
                System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cert.close();
            }

            // creating a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // creating a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // creating an SSLSocketFactory that uses our TrustManager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
            client.setSslSocketFactory(sslContext.getSocketFactory());
            client.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    if (hostname.equals(hostName)) {
                        return true;
                    }

                    HostnameVerifier hv =
                            HttpsURLConnection.getDefaultHostnameVerifier();
                    return hv.verify(hostname, session);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return client;
    }


    public LMISRestApi getLmisRestApi() {
        return lmisRestApi;
    }

    private GsonConverter registerTypeAdapter() {
        return new GsonConverter(new GsonBuilder()
                .registerTypeAdapter(RnRForm.class, getTypeAdapter())
                .registerTypeAdapter(Product.class, new ProductsAdapter())
                .create());
    }

    @NonNull
    private JsonSerializer getTypeAdapter() {
        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_sync_back_rnr_186)) {
            return new RnrFormAdapter();
        } else {
            return new RnrFormAdapterForFeatureToggle();
        }
    }

    @Data
    public static class UserResponse {
        User userInformation;
    }

    class MyErrorHandler implements ErrorHandler {
        @Override
        public Throwable handleError(RetrofitError cause) {
            Response r = cause.getResponse();
            if (r != null && r.getStatus() == 401) {
                return new UnauthorizedException(cause);
            }
            return cause;
        }
    }

}
