package org.openlmis.core.network;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.Collections;

import retrofit.client.Client;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;

public class LMISRestManagerMock extends LMISRestManager {

    private static Client mockClient;

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
}
