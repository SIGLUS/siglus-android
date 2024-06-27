package org.openlmis.core.model;

import static org.junit.Assert.assertEquals;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

import org.junit.Test;

public class PodProductItemTest {

  @Test
  public void test_getSumShippedQuantity() {
    // given
    PodProductItem podProductItem = new PodProductItem();

    PodProductLotItem podProductLotItem = new PodProductLotItem();
    podProductLotItem.setShippedQuantity(5L);

    podProductItem.setPodProductLotItemsWrapper(newArrayList(podProductLotItem, podProductLotItem));

    // when
    long actualQuantity = podProductItem.getSumShippedQuantity();
    // then
    assertEquals(10, actualQuantity);
  }
}