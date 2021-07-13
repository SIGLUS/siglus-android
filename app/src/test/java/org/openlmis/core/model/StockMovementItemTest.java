package org.openlmis.core.model;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

import java.text.ParseException;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.model.builder.StockMovementItemBuilder;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;

@SuppressWarnings("PMD")
public class StockMovementItemTest {

  private StockMovementItem stockMovementItem;

  @Before
  public void setUp() throws Exception {
    stockMovementItem = new StockMovementItem();
    stockMovementItem.setStockOnHand(100);
    stockMovementItem.setMovementQuantity(50);
  }

  @Test
  public void shouldSetInitialAmountCorrectWhenTypeIsIssue() throws Exception {
    //given
    stockMovementItem.setMovementType(MovementReasonManager.MovementType.ISSUE);

    //when
    long calculatePreviousSOH = stockMovementItem.calculatePreviousSOH();

    //then
    assertThat(calculatePreviousSOH, is(150L));
  }

  @Test
  public void shouldSetInitialAmountCorrectWhenTypeIsNegative() throws Exception {
    //given
    stockMovementItem.setMovementType(MovementReasonManager.MovementType.NEGATIVE_ADJUST);

    //when
    long calculatePreviousSOH = stockMovementItem.calculatePreviousSOH();

    //then
    assertThat(calculatePreviousSOH, is(150L));
  }

  @Test
  public void shouldSetInitialAmountCorrectWhenTypeIsReceive() throws Exception {
    //given
    stockMovementItem.setMovementType(MovementReasonManager.MovementType.RECEIVE);

    //when
    long calculatePreviousSOH = stockMovementItem.calculatePreviousSOH();

    //then
    assertThat(calculatePreviousSOH, is(50L));
  }

  @Test
  public void shouldSetInitialAmountCorrectWhenTypeIsPositive() throws Exception {
    //given
    stockMovementItem.setMovementType(MovementReasonManager.MovementType.POSITIVE_ADJUST);

    //when
    long calculatePreviousSOH = stockMovementItem.calculatePreviousSOH();

    //then
    assertThat(calculatePreviousSOH, is(50L));
  }

  @Test
  public void shouldSetInitialAmountCorrectWhenTypeIsPhysical() throws Exception {
    //given
    stockMovementItem.setMovementType(MovementReasonManager.MovementType.PHYSICAL_INVENTORY);
    stockMovementItem.setMovementQuantity(0L);

    //when
    long calculatePreviousSOH = stockMovementItem.calculatePreviousSOH();

    //then
    assertThat(calculatePreviousSOH, is(100L));
  }

  @Test
  public void shouldPopulateAllLotQuantitiesAndCalculateNewSOHForStockItem() throws ParseException {
    LotMovementViewModel lotMovementViewModel1 = new LotMovementViewModel("ABC", "Dec 2016", "10",
        MovementReasonManager.MovementType.ISSUE);
    lotMovementViewModel1.setQuantity("10");
    LotMovementViewModel lotMovementViewModel2 = new LotMovementViewModel("DEF", "Nov 2016", "20",
        MovementReasonManager.MovementType.ISSUE);
    lotMovementViewModel2.setQuantity("15");
    LotMovementViewModel lotMovementViewModel3 = new LotMovementViewModel("HIJ", "Oct 2016", "30",
        MovementReasonManager.MovementType.ISSUE);
    lotMovementViewModel3.setQuantity("5");

    List<LotMovementViewModel> lotMovementViewModelList = newArrayList(lotMovementViewModel1,
        lotMovementViewModel2, lotMovementViewModel3);

    StockCard stockCard = StockCardBuilder.buildStockCard();
    StockMovementItem stockMovementItem = new StockMovementItemBuilder()
        .withMovementDate("2016-11-30").withStockOnHand(100).build();
    stockMovementItem.setStockCard(stockCard);
    stockMovementItem.populateLotQuantitiesAndCalculateNewSOH(lotMovementViewModelList
    );
    assertThat(stockMovementItem.getStockOnHand(), is(70L));
    assertThat(stockMovementItem.getMovementQuantity(), is(30L));
  }

  @Test
  public void shouldPopulateLotQuantitiesAndCalculateNewSOH() throws ParseException {
    LotMovementViewModel lotMovementViewModel1 = new LotMovementViewModel("ABC", "Dec 2016", "10",
        MovementReasonManager.MovementType.ISSUE);
    lotMovementViewModel1.setQuantity("10");
    LotMovementViewModel lotMovementViewModel2 = new LotMovementViewModel("DEF", "Nov 2016", "20",
        MovementReasonManager.MovementType.ISSUE);
    lotMovementViewModel2.setQuantity("15");
    LotMovementViewModel lotMovementViewModel3 = new LotMovementViewModel("HIJ", "Oct 2016", "50",
        MovementReasonManager.MovementType.ISSUE);
    lotMovementViewModel3.setQuantity("5");

    List<LotMovementViewModel> lotMovementViewModelList = newArrayList(lotMovementViewModel1,
        lotMovementViewModel2, lotMovementViewModel3);

    StockCard stockCard = StockCardBuilder.buildStockCard();
    stockCard.setStockOnHand(80L);
    StockMovementItem stockMovementItem = new StockMovementItemBuilder()
        .withMovementDate("2016-11-30").build();
    stockMovementItem.setStockOnHand(80L);
    stockMovementItem.setStockCard(stockCard);

    stockMovementItem.populateLotQuantitiesAndCalculateNewSOH(lotMovementViewModelList
    );
    assertThat(stockMovementItem.getMovementQuantity(), is(30L));
    assertThat(stockMovementItem.getStockOnHand(), is(50L));
    assertThat(stockMovementItem.getLotMovementItemListWrapper().get(0).getMovementQuantity(),
        is(-10L));
    assertThat(stockMovementItem.getLotMovementItemListWrapper().get(1).getMovementQuantity(),
        is(-15L));
    assertThat(stockMovementItem.getLotMovementItemListWrapper().get(2).getMovementQuantity(),
        is(-5L));
  }

  @Test
  public void populateLotAndResetStockOnHandOfLotAccordingPhysicalAdjustment()
      throws ParseException {
    LotMovementViewModel lotMovementViewModel1 = new LotMovementViewModel("ABC", "Dec 2016", "10",
        MovementReasonManager.MovementType.PHYSICAL_INVENTORY);
    lotMovementViewModel1.setQuantity("10");
    LotMovementViewModel lotMovementViewModel2 = new LotMovementViewModel("DEF", "Nov 2016", "20",
        MovementReasonManager.MovementType.PHYSICAL_INVENTORY);
    lotMovementViewModel2.setQuantity("15");
    LotMovementViewModel lotMovementViewModel3 = new LotMovementViewModel("HIJ", "Oct 2016", "30",
        MovementReasonManager.MovementType.PHYSICAL_INVENTORY);
    lotMovementViewModel3.setQuantity("5");

    LotMovementViewModel lotMovementViewModelNew = new LotMovementViewModel("HIJ", "Oct 2016", "0L",
        MovementReasonManager.MovementType.PHYSICAL_INVENTORY);
    lotMovementViewModelNew.setQuantity("10");

    List<LotMovementViewModel> lotMovementViewModelList = newArrayList(lotMovementViewModel1,
        lotMovementViewModel2, lotMovementViewModel3);
    List<LotMovementViewModel> newLotMovementViewModelList = newArrayList(lotMovementViewModelNew);

    StockCard stockCard = StockCardBuilder.buildStockCard();
    StockMovementItem stockMovementItem = new StockMovementItemBuilder()
        .withMovementDate("2016-11-30").build();
    stockMovementItem.setStockCard(stockCard);
    stockMovementItem
        .populateLotAndResetStockOnHandOfLotAccordingPhysicalAdjustment(lotMovementViewModelList,
            newLotMovementViewModelList);
    assertThat(stockMovementItem.getLotMovementItemListWrapper().get(0).getStockOnHand(), is(10L));
    assertThat(stockMovementItem.getLotMovementItemListWrapper().get(1).getMovementQuantity(),
        is(-5L));
    assertThat(stockMovementItem.getLotMovementItemListWrapper().get(2).getMovementQuantity(),
        is(-25L));
    assertThat(
        stockMovementItem.getNewAddedLotMovementItemListWrapper().get(0).getMovementQuantity(),
        is(10L));
  }

}