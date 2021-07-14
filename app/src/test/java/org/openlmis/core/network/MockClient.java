package org.openlmis.core.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
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
    System.out.println("1 +++++++++++ request = [" + requestUrl + "]");
    // The following port number should be same with the ENV which you running
    requestUrl = requestUrl.split(LMISApp.getInstance().getResources().getString(R.integer.server_base_url_port))[1];
    // single debug api use follow code
//    requestUrl = requestUrl.split("\\.us")[1];
    System.out.println("+++++++++++ request = [" + requestUrl + "]");
    if (urlToResponseMap.containsKey(requestUrl)) {
      return urlToResponseMap.get(requestUrl);
    }
    throw new IOException(requestUrl);
  }

  public static MockClient MockClientBuilder() {
    return new MockClient();
  }

  public MockClient addMockedResponse(String requestUrl, int mockedStatus, String mockedReason,
      byte[] responseBody) {
    Response response = new Response(requestUrl, mockedStatus, mockedReason,
        new ArrayList<Header>(),
        new TypedByteArray("application/json", responseBody));
    urlToResponseMap.put(requestUrl, response);
    return this;
  }
}
