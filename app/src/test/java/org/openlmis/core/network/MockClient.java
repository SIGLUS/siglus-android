package org.openlmis.core.network;

import org.mockito.Mock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit.client.Client;
import retrofit.client.Header;
import retrofit.client.Request;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;

public class MockClient implements Client {

    Map<String, Response> urlToResponseMap = new HashMap<>();

    @Override
    public Response execute(Request request) throws IOException {
        String requestUrl = request.getUrl();
        requestUrl = requestUrl.split("9091")[1];
        if (urlToResponseMap.keySet().contains(requestUrl)) {
            return urlToResponseMap.get(requestUrl);
        }
        return null;
    }

    public static MockClient MockClientBuilder() {
        return new MockClient();
    }

    public MockClient addMockedResponse(String requestUrl, int mockedStatus, String mockedReason, byte[] responseBody) {
        Response response = new Response(requestUrl, mockedStatus, mockedReason, new ArrayList<Header>(),
                new TypedByteArray("application/json", responseBody));
        urlToResponseMap.put(requestUrl, response);
        return this;
    }
}
