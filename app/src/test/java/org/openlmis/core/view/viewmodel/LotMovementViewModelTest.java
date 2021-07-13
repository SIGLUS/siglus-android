package org.openlmis.core.view.viewmodel;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.openlmis.core.utils.DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import org.junit.Test;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.LotMovementItem;
import org.openlmis.core.model.Product;

@SuppressWarnings("PMD")
public class LotMovementViewModelTest {

  private final LotMovementViewModel viewModel = new LotMovementViewModel();

  @Test
  public void shouldReturnTrueWhenViewModelIsValid() {
    viewModel.setLotNumber("ei-233");
    viewModel.setExpiryDate("2013-09-01");
    viewModel.setQuantity("233");

    assertTrue(viewModel.isValid());
  }

  @Test
  public void shouldReturnFalseWhenViewModelIsInvalid() {
    viewModel.setLotSoh("100");
    viewModel.setQuantity("300");
    viewModel.setExpiryDate("2014-03-18");
    viewModel.setLotNumber("100");
    viewModel.validateLotWithPositiveQuantity();
    assertTrue(viewModel.isValid());

    viewModel.setLotNumber("dk-23");
    viewModel.validateLotWithPositiveQuantity();
    assertTrue(viewModel.isValid());

    viewModel.setQuantity("333");
    viewModel.validateLotWithPositiveQuantity();
    assertTrue(viewModel.isValid());

    viewModel.setExpiryDate("2014-03-18");
    viewModel.validateLotWithPositiveQuantity();
    assertTrue(viewModel.isValid());
  }

  @Test
  public void shouldReturnFalseWhenIssueQuantityGreaterThanSOH() {
    viewModel.setLotSoh("100");
    viewModel.setQuantity("300");
    viewModel.setExpiryDate("2014-03-18");
    viewModel.setLotNumber("100");
    viewModel.validateLotWithPositiveQuantity();
    viewModel.setMovementType(MovementReasonManager.MovementType.ISSUE);
    assertFalse(viewModel.validateQuantityNotGreaterThanSOH());
  }

  @Test
  public void shouldConvertLotMovementItemWithRightExpiryDate() {
    viewModel.setQuantity("10");
    viewModel.setLotNumber("lot1");
    viewModel.setExpiryDate("Feb 2015");

    LotMovementItem lotMovementItem = viewModel.convertViewToModel(new Product());
    assertEquals(new GregorianCalendar(2015, 1, 28).getTime(),
        lotMovementItem.getLot().getExpirationDate());
  }

  @Test
  public void shouldConvertLotMovementItemWithRightStockOnHand() {
    viewModel.setQuantity("10");
    viewModel.setLotNumber("lot1");
    viewModel.setExpiryDate("Feb 2015");
    viewModel.setLotSoh("20");

    LotMovementItem lotMovementItem = viewModel.convertViewToModelAndResetSOH(new Product());
    assertThat(lotMovementItem.getStockOnHand(), is(10L));
  }

  @Test
  public void shouldGenerateLotNumberForProductWithoutLot() {
    String productCode = "02F49";
    String expiryDate = "Nov 2017";

    assertThat(LotMovementViewModel.generateLotNumberForProductWithoutLot(productCode, expiryDate),
        is("SEM-LOTE-02F49-112017"));
  }

  @Test
  public void shouldGetAdjustmentQuantity() {
    viewModel.setQuantity("1");
    viewModel.setLotSoh(null);
    assertEquals(1, viewModel.getAdjustmentQuantity());

    viewModel.setLotSoh("1");
    assertEquals(0, viewModel.getAdjustmentQuantity());

    viewModel.setQuantity("10");
    assertEquals(9, viewModel.getAdjustmentQuantity());
  }

  @Test
  public void shouldExpiredLot() {
    Calendar expireLot = Calendar.getInstance();
    expireLot.add(Calendar.MONTH, -2);
    DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_ONLY_MONTH_AND_YEAR);
    viewModel.setExpiryDate(dateFormat.format(expireLot.getTime()));
    assertTrue(viewModel.isExpiredLot());
  }

  @Test
  public void shouldNotExpiredLot() {
    Calendar expireLot = Calendar.getInstance();
    DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_ONLY_MONTH_AND_YEAR);
    viewModel.setExpiryDate(dateFormat.format(expireLot.getTime()));
    assertFalse(viewModel.isExpiredLot());
  }
}