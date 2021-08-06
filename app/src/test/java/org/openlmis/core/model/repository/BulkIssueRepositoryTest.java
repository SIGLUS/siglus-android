/*
 * This program is part of the OpenLMIS logistics management information
 * system platform software.
 *
 * Copyright © 2015 ThoughtWorks, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should
 * have received a copy of the GNU Affero General Public License along with
 * this program. If not, see http://www.gnu.org/licenses. For additional
 * information contact info@OpenLMIS.org
 */

package org.openlmis.core.model.repository;

import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.DraftBulkIssueProduct;
import org.openlmis.core.model.DraftBulkIssueProductLotItem;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.utils.DateUtil;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class BulkIssueRepositoryTest {

  BulkIssueRepository bulkIssueRepository;

  @Before
  public void setUp() throws Exception {
    bulkIssueRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(BulkIssueRepository.class);
  }

  @Test
  public void shouldHasDraftAfterSave() throws LMISException {
    // given
    DraftBulkIssueProduct draftProduct = getDraftProduct();
    List<DraftBulkIssueProductLotItem> draftLots = getDraftLots(draftProduct);
    draftProduct.setDraftLotItemListWrapper(draftLots);
    bulkIssueRepository.saveDraft(Collections.singletonList(draftProduct));

    // when
    boolean hasDraft = bulkIssueRepository.hasDraft();

    // then
    Assert.assertTrue(hasDraft);
  }

  @Test
  public void shouldCorrectSaveDraft() throws LMISException {
    // given
    DraftBulkIssueProduct draftProduct = getDraftProduct();
    List<DraftBulkIssueProductLotItem> draftLots = getDraftLots(draftProduct);
    draftProduct.setDraftLotItemListWrapper(draftLots);

    // when
    bulkIssueRepository.saveDraft(Collections.singletonList(draftProduct));

    // then
    List<DraftBulkIssueProduct> draftBulkIssueProducts = bulkIssueRepository.queryAllBulkIssueDraft();
    Assert.assertEquals(1, draftBulkIssueProducts.size());

    DraftBulkIssueProduct actualDraftProduct = draftBulkIssueProducts.get(0);
    actualDraftProduct.setProduct(draftProduct.getProduct());
    Assert.assertEquals(draftProduct, actualDraftProduct);

    Assert.assertEquals(1, draftBulkIssueProducts.get(0).getDraftLotItemListWrapper().size());

    Assert.assertEquals(draftLots.get(0), draftBulkIssueProducts.get(0).getDraftLotItemListWrapper().get(0));
  }

  @Test
  public void shouldCorrectDeleteDraft() throws LMISException {
    // given
    DraftBulkIssueProduct draftProduct = getDraftProduct();
    List<DraftBulkIssueProductLotItem> draftLots = getDraftLots(draftProduct);
    draftProduct.setDraftLotItemListWrapper(draftLots);
    bulkIssueRepository.saveDraft(Collections.singletonList(draftProduct));

    // when
    bulkIssueRepository.deleteDraft();

    // then
    Assert.assertFalse(bulkIssueRepository.hasDraft());
  }

  private DraftBulkIssueProduct getDraftProduct() {
    return DraftBulkIssueProduct.builder()
        .documentNumber("123")
        .done(false)
        .movementReasonCode("movementReason")
        .product(ProductBuilder.buildAdultProduct())
        .requested(123L)
        .build();
  }

  private List<DraftBulkIssueProductLotItem> getDraftLots(DraftBulkIssueProduct draftBulkIssueProduct) {
    return Collections.singletonList(DraftBulkIssueProductLotItem.builder()
        .amount(1L)
        .draftBulkIssueProduct(draftBulkIssueProduct)
        .expirationDate(DateUtil.parseString("2021-08-06", DateUtil.DB_DATE_FORMAT))
        .lotNumber("lotNumber")
        .lotSoh(10)
        .build());
  }
}