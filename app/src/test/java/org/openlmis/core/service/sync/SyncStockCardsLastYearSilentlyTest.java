package org.openlmis.core.service.sync;

import static org.mockito.Mockito.mock;

import com.google.inject.AbstractModule;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.User;
import org.openlmis.core.model.repository.UserRepository;
import org.openlmis.core.network.LMISRestApi;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;
import rx.observers.TestSubscriber;

@RunWith(LMISTestRunner.class)
public class SyncStockCardsLastYearSilentlyTest {

  private final LMISRestApi restApi = mock(LMISRestApi.class);
  private SyncStockCardsLastYearSilently syncStockCardsLastYearSilently;
  private User defaultUser;
  private UserRepository userRepository;

  @Before
  public void setUp() {
//        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
    syncStockCardsLastYearSilently = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(SyncStockCardsLastYearSilently.class);
    userRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(UserRepository.class);
    defaultUser = new User();
    defaultUser.setUsername("cs_gelo");
    defaultUser.setPassword("password");
    defaultUser.setFacilityId("808");
    defaultUser.setFacilityName("CS Gelo");
    defaultUser.setFacilityCode("HF615");
    userRepository.createOrUpdate(defaultUser);
    UserInfoMgr.getInstance().setUser(defaultUser);
  }

  @Test
  public void shouldSyncLastYearStockCards() {

    syncStockCardsLastYearSilently.performSync().subscribe(new GetOnCompleteSubscriber());
  }

  private class MyTestModule extends AbstractModule {

    @Override
    protected void configure() {
      bind(LMISRestApi.class).toInstance(restApi);
      bind(UserRepository.class).toInstance(userRepository);
    }
  }

  private class GetOnCompleteSubscriber extends TestSubscriber<List<StockCard>> {

    @Override
    public void onCompleted() {
      super.onCompleted();
    }
  }

}
