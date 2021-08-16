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
import org.openlmis.core.model.DraftBulkIssueLot;
import org.openlmis.core.model.DraftBulkIssueProduct;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.builder.LotBuilder;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.utils.DateUtil;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class BulkIssueRepositoryTest {

  BulkIssueRepository bulkIssueRepository;

  ProductRepository productRepository;

  StockRepository stockRepository;

  LotRepository lotRepository;

  private StockCard stockCard;

  @Before
  public void setUp() throws Exception {
    bulkIssueRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(BulkIssueRepository.class);
    productRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(ProductRepository.class);
    stockRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(StockRepository.class);
    lotRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(LotRepository.class);
    prepareData();
  }

  @Test
  public void shouldHasDraftAfterSave() throws LMISException {
    // given
    DraftBulkIssueProduct draftProduct = getDraftProduct();
    List<DraftBulkIssueLot> draftLots = getDraftLots(draftProduct);
    draftProduct.setDraftLotListWrapper(draftLots);
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
    List<DraftBulkIssueLot> draftLots = getDraftLots(draftProduct);
    draftProduct.setDraftLotListWrapper(draftLots);

    // when
    bulkIssueRepository.saveDraft(Collections.singletonList(draftProduct));

    // then
    List<DraftBulkIssueProduct> draftBulkIssueProducts = bulkIssueRepository.queryUsableProductAndLotDraft();
    Assert.assertEquals(1, draftBulkIssueProducts.size());

    DraftBulkIssueProduct actualDraftProduct = draftBulkIssueProducts.get(0);
    actualDraftProduct.setStockCard(draftProduct.getStockCard());
    Assert.assertEquals(draftProduct, actualDraftProduct);

    Assert.assertEquals(1, draftBulkIssueProducts.get(0).getDraftLotListWrapper().size());

    Assert.assertEquals(draftLots.get(0), draftBulkIssueProducts.get(0).getDraftLotListWrapper().get(0));
  }

  @Test
  public void shouldCorrectDeleteDraft() throws LMISException {
    // given
    DraftBulkIssueProduct draftProduct = getDraftProduct();
    List<DraftBulkIssueLot> draftLots = getDraftLots(draftProduct);
    draftProduct.setDraftLotListWrapper(draftLots);
    bulkIssueRepository.saveDraft(Collections.singletonList(draftProduct));

    // when
    bulkIssueRepository.deleteDraft();

    // then
    Assert.assertFalse(bulkIssueRepository.hasDraft());
  }

  private void prepareData() throws LMISException {
    Product product = ProductBuilder.buildAdultProduct();
    product.setActive(true);
    productRepository.createOrUpdate(product);
    stockCard = new StockCardBuilder()
        .setProduct(product)
        .setStockOnHand(100)
        .build();
    stockRepository.createOrUpdate(stockCard);
    LotOnHand lotOnHand = new LotOnHand();
    lotOnHand.setStockCard(stockCard);
    lotOnHand.setQuantityOnHand(100L);
    lotOnHand.setLot(LotBuilder.create()
        .setExpirationDate(DateUtil.parseString("2021-08-06", DateUtil.DB_DATE_FORMAT))
        .setLotNumber("LotNumber")
        .setProduct(product)
        .build());
    lotRepository.createOrUpdateLotsInformation(Collections.singletonList(lotOnHand));
  }

  private DraftBulkIssueProduct getDraftProduct() {
    return DraftBulkIssueProduct.builder()
        .documentNumber("123")
        .done(false)
        .movementReasonCode("movementReason")
        .stockCard(stockCard)
        .requested(10L)
        .build();
  }

  private List<DraftBulkIssueLot> getDraftLots(DraftBulkIssueProduct draftBulkIssueProduct) {
    return Collections.singletonList(DraftBulkIssueLot.builder()
        .amount(1L)
        .draftBulkIssueProduct(draftBulkIssueProduct)
        .lotNumber("123")
        .build());
  }
}