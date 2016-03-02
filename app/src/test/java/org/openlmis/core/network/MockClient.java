package org.openlmis.core.network;

import java.io.IOException;
import java.util.ArrayList;

import retrofit.client.Client;
import retrofit.client.Header;
import retrofit.client.Request;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;

public class MockClient implements Client {

    private String url;
    private byte[] responseBody;
    private int status;
    private String reason;

    @Override
    public Response execute(Request request) throws IOException {
        if (request.getUrl().contains(url)) {
            ArrayList<Header> headers = new ArrayList<>();
            return new Response(url, status, reason, headers,
                    new TypedByteArray("application/json", responseBody));
        }
        return null;
    }

    public static MockClient MockClientBuilder() {
        return new MockClient();
    }

    public MockClient withUrl(String url) {
        this.url = url;
        return this;
    }

    public MockClient withStatusAndReason(int status, String reason) {
        this.status = status;
        this.reason = reason;
        return this;
    }

    public MockClient withResponseBody(byte[] body) {
        this.responseBody = body;
        return this;
    }


}
