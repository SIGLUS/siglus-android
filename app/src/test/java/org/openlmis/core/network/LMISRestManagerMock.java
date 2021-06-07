package org.openlmis.core.network;

import android.content.Context;
import androidx.annotation.NonNull;

import com.squareup.okhttp.OkHttpClient;

import org.openlmis.core.model.User;

import java.util.Collections;

import retrofit.RetrofitError;
import retrofit.client.Client;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;

public class LMISRestManagerMock extends LMISRestManager {

    protected static Client mockClient;
    public OkHttpClient okHttpClient;

    protected LMISRestManagerMock(Context context) {
        super(context);
    }

    @NonNull
    @Override
    protected Client getSSLClient() {
        return mockClient;
    }

    @NonNull
    public static LMISRestManagerMock getRestManagerWithMockClient(String url, int status, String reason, String json, Context context) {
        mockClient = MockClient.MockClientBuilder().addMockedResponse(url, status, reason, json.getBytes());
        return new LMISRestManagerMock(context);
    }

    public void addNewMockedResponse(String url, int status, String reason, String json) {
        ((MockClient) mockClient).addMockedResponse(url, status, reason, json.getBytes());
    }

    public static Response createDummyJsonResponse(String url, int responseCode, String reason, String json) {
        return new Response(url, responseCode, reason, Collections.EMPTY_LIST,
                new TypedByteArray("application/json", json.getBytes()));
    }

    @Override
    protected OkHttpClient getOkHttpClient() {
        okHttpClient = super.getOkHttpClient();
        return okHttpClient;
    }

    protected Client superGetSSLClient() {
        return super.getSSLClient();
    }

    @Override
    public void refreshUserAuthorize(User user, RetrofitError cause) {
        super.refreshUserAuthorize(user, cause);
    }
}
