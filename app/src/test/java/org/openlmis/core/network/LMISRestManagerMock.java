package org.openlmis.core.network;

import android.support.annotation.NonNull;

import retrofit.client.Client;

public class LMISRestManagerMock extends LMISRestManager {

    private static Client mockClient;

    @NonNull
    @Override
    protected Client getSSLClient() {
        return mockClient;
    }

    @NonNull
    public static LMISRestManagerMock getRestManagerWithMockClient(String url, int status, String reason, String json) {
        mockClient = MockClient.MockClientBuilder().withUrl(url).withStatusAndReason(status, reason).withResponseBody(json.getBytes());
        return new LMISRestManagerMock();
    }
}
