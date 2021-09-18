package org.openlmis.core.network;

import android.content.Context;
import androidx.annotation.NonNull;
import com.squareup.okhttp.OkHttpClient;
import org.openlmis.core.model.User;
import retrofit.RetrofitError;
import retrofit.client.Client;

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
  public static LMISRestManagerMock getRestManagerWithMockClient(String url, int status,
      String reason, String json, Context context) {
    mockClient = MockClient.MockClientBuilder()
        .addMockedResponse(url, status, reason, json.getBytes());
    return new LMISRestManagerMock(context);
  }

  public void addNewMockedResponse(String url, int status, String reason, String json) {
    ((MockClient) mockClient).addMockedResponse(url, status, reason, json.getBytes());
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
  public void refreshAccessToken(User user, RetrofitError cause) {
    super.refreshAccessToken(user, cause);
  }
}
