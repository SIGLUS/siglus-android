package org.openlmis.core.model.repository;

import static junit.framework.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.BaseInfoItem;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class BaseInfoItemRepositoryTest {

  private BaseInfoItemRepository baseInfoItemRepository;

  @Before
  public void setUp() throws Exception {
    baseInfoItemRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(BaseInfoItemRepository.class);
  }

  @Test
  public void shouldBatchCreateOrUpdateBaseInfoItems() throws Exception {
    List<BaseInfoItem> baseInfoItems = Arrays.asList(
        new BaseInfoItem(), new BaseInfoItem(), new BaseInfoItem());

    baseInfoItemRepository.batchCreateOrUpdate(baseInfoItems);

    assertEquals(3, baseInfoItemRepository.genericDao.queryForAll().size());
  }

  @Test
  public void shouldBatchDeleteBaseInfoItems() throws Exception {
    List<BaseInfoItem> baseInfoItems = Arrays.asList(
        new BaseInfoItem(), new BaseInfoItem(), new BaseInfoItem());
    baseInfoItemRepository.batchCreateOrUpdate(baseInfoItems);

    baseInfoItemRepository.batchDelete(baseInfoItems);
    assertEquals(0, baseInfoItemRepository.genericDao.queryForAll().size());
  }
}