package org.openlmis.core.view.viewmodel;

import static org.junit.Assert.assertEquals;

import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.view.viewmodel.BulkEntriesViewModel.ValidationType;

@RunWith(LMISTestRunner.class)
public class BulkEntriesViewModelTest {

  private final String lotNumber1 = "lot 1";
  private final String lotNumber2 = "lot 2";
  private final String quantity = "100";
  private final String movementReason = "DDM";
  private final String documentNumber = "1";
  private final String lotSoh = "10";
  private final String expiryDate = "2021-07";
  private BulkEntriesViewModel bulkEntriesViewModel;

  @Before
  public void setUp() throws Exception {
    StockCard stockCard = StockCardBuilder.buildStockCard();
    stockCard.setId(1);
    bulkEntriesViewModel = new BulkEntriesViewModel(stockCard);
  }

  @Test
  public void shouldGetGreenName() {
    // when
    SpannableStringBuilder spannableStringBuilder = bulkEntriesViewModel.getGreenName();
    // then
    assertEquals(0, spannableStringBuilder.getSpanFlags(ForegroundColorSpan.class));
  }

  @Test
  public void shouldCalculateStockOnHand() {
    // given
    LotMovementViewModel existingLotMovementViewModel = LotMovementViewModel.builder()
        .lotNumber(lotNumber1)
        .lotSoh(lotSoh)
        .quantity(quantity)
        .build();
    LotMovementViewModel newLotMovementViewModel = LotMovementViewModel.builder()
        .lotNumber(lotNumber2)
        .quantity(quantity)
        .build();

    bulkEntriesViewModel.getExistingLotMovementViewModelList().add(existingLotMovementViewModel);
    bulkEntriesViewModel.getNewLotMovementViewModelList().add(newLotMovementViewModel);

    // when
    bulkEntriesViewModel.calculateLotOnHand();

    // then
    long expectedLotOnHand = Long.parseLong(lotSoh) + Long.parseLong(quantity);
    assertEquals(expectedLotOnHand,
        Long.parseLong(bulkEntriesViewModel.getExistingLotMovementViewModelList().get(0).getLotSoh()));
    assertEquals(quantity, bulkEntriesViewModel.getNewLotMovementViewModelList().get(0).getLotSoh());
  }

  @Test
  public void shouldSetDefaultReasonForNoAmountLot() {
    // given
    LotMovementViewModel existingLotMovementViewModel = LotMovementViewModel.builder()
        .lotNumber(lotNumber1)
        .lotSoh(lotSoh)
        .build();
    bulkEntriesViewModel.getExistingLotMovementViewModelList().add(existingLotMovementViewModel);

    // when
    bulkEntriesViewModel.setDefaultReasonForNoAmountLot(Constants.DEFAULT_REASON_FOR_NO_AMOUNT_LOT);

    // then
    assertEquals(Constants.DEFAULT_REASON_FOR_NO_AMOUNT_LOT,
        bulkEntriesViewModel.getExistingLotMovementViewModelList().get(0).getMovementReason());
  }


  @Test
  public void validationTypeShouldBeNoLotWhenValidate() {
    // given
    bulkEntriesViewModel.existingLotMovementViewModelList = new ArrayList<>();
    bulkEntriesViewModel.newLotMovementViewModelList = new ArrayList<>();

    // when
    bulkEntriesViewModel.validate();

    // then
    assertEquals(ValidationType.NO_LOT, bulkEntriesViewModel.getValidationType());
  }

  @Test
  public void validationTypeShouldBeExistingLotAllAmountBlank() {
    // given
    LotMovementViewModel lotMovementViewModel = LotMovementViewModel.builder()
        .lotNumber(lotNumber1)
        .build();
    bulkEntriesViewModel.getExistingLotMovementViewModelList().add(lotMovementViewModel);
    bulkEntriesViewModel.newLotMovementViewModelList = new ArrayList<>();

    // when
    bulkEntriesViewModel.validate();

    // then
    assertEquals(ValidationType.EXISTING_LOT_ALL_AMOUNT_BLANK, bulkEntriesViewModel.getValidationType());
  }

  @Test
  public void validationTypeShouldBeExistingLotInfoHasBlankWhenNewLLotListEmpty() {
    // given
    LotMovementViewModel lotMovementViewModel = LotMovementViewModel.builder()
        .lotNumber(lotNumber1)
        .quantity("100")
        .build();
    bulkEntriesViewModel.getExistingLotMovementViewModelList().add(lotMovementViewModel);
    bulkEntriesViewModel.newLotMovementViewModelList = new ArrayList<>();

    // when
    bulkEntriesViewModel.validate();

    // then
    assertEquals(ValidationType.EXISTING_LOT_INFO_HAS_BLANK, bulkEntriesViewModel.getValidationType());
  }

  @Test
  public void validationTypeShouldBeExistingLotInfoHasBlank() {
    // given
    LotMovementViewModel existingLotMovementViewModel = LotMovementViewModel.builder()
        .lotNumber(lotNumber1)
        .quantity(quantity)
        .build();
    LotMovementViewModel newLotMovementViewModel = LotMovementViewModel.builder()
        .lotNumber(lotNumber2)
        .lotSoh(lotSoh)
        .quantity(quantity)
        .movementReason(movementReason)
        .documentNumber(documentNumber)
        .expiryDate(expiryDate)
        .build();
    bulkEntriesViewModel.getExistingLotMovementViewModelList().add(existingLotMovementViewModel);
    bulkEntriesViewModel.getNewLotMovementViewModelList().add(newLotMovementViewModel);

    // when
    bulkEntriesViewModel.validate();

    // then
    assertEquals(ValidationType.EXISTING_LOT_INFO_HAS_BLANK, bulkEntriesViewModel.getValidationType());
  }

  @Test
  public void validationTypeShouldBeNewLotBlankWithExistingLotListEmpty() {
    // given
    LotMovementViewModel newLotMovementViewModel = LotMovementViewModel.builder()
        .lotNumber(lotNumber2)
        .lotSoh(lotSoh)
        .movementReason(movementReason)
        .documentNumber(documentNumber)
        .expiryDate(expiryDate)
        .build();
    bulkEntriesViewModel.existingLotMovementViewModelList = new ArrayList<>();
    bulkEntriesViewModel.getNewLotMovementViewModelList().add(newLotMovementViewModel);

    // when
    bulkEntriesViewModel.validate();

    // then
    assertEquals(ValidationType.NEW_LOT_BLANK, bulkEntriesViewModel.getValidationType());

  }

  @Test
  public void validationTypeShouldBeNewLotBlank() {
    // given
    LotMovementViewModel existingLotMovementViewModel = LotMovementViewModel.builder()
        .lotNumber(lotNumber1)
        .quantity(quantity)
        .build();
    LotMovementViewModel newLotMovementViewModel = LotMovementViewModel.builder()
        .lotNumber(lotNumber2)
        .lotSoh(lotSoh)
        .movementReason(movementReason)
        .documentNumber(documentNumber)
        .expiryDate(expiryDate)
        .build();

    bulkEntriesViewModel.getExistingLotMovementViewModelList().add(existingLotMovementViewModel);
    bulkEntriesViewModel.getNewLotMovementViewModelList().add(newLotMovementViewModel);

    // when
    bulkEntriesViewModel.validate();

    // then
    assertEquals(ValidationType.NEW_LOT_BLANK, bulkEntriesViewModel.getValidationType());
  }

  @Test
  public void validationTypeShouldBeValid() {
    // given
    LotMovementViewModel existingLotMovementViewModel = LotMovementViewModel.builder()
        .lotNumber(lotNumber1)
        .lotSoh(lotSoh)
        .movementReason(movementReason)
        .documentNumber(documentNumber)
        .expiryDate(expiryDate)
        .quantity(quantity)
        .build();
    LotMovementViewModel newLotMovementViewModel = LotMovementViewModel.builder()
        .lotNumber(lotNumber2)
        .lotSoh(lotSoh)
        .movementReason(movementReason)
        .documentNumber(documentNumber)
        .expiryDate(expiryDate)
        .quantity(quantity)
        .build();

    bulkEntriesViewModel.getExistingLotMovementViewModelList().add(existingLotMovementViewModel);
    bulkEntriesViewModel.getNewLotMovementViewModelList().add(newLotMovementViewModel);

    // when
    bulkEntriesViewModel.validate();

    // then
    assertEquals(ValidationType.VALID, bulkEntriesViewModel.getValidationType());

  }

  @Test
  public void validationTypeShouldBeValidWhenOnlyHasExistingLot() {
    // given
    LotMovementViewModel existingLotMovementViewModel = LotMovementViewModel.builder()
        .lotNumber(lotNumber1)
        .lotSoh(lotSoh)
        .movementReason(movementReason)
        .documentNumber(documentNumber)
        .expiryDate(expiryDate)
        .quantity(quantity)
        .build();
    bulkEntriesViewModel.getExistingLotMovementViewModelList().add(existingLotMovementViewModel);
    bulkEntriesViewModel.newLotMovementViewModelList = new ArrayList<>();

    // when
    bulkEntriesViewModel.validate();

    // then
    assertEquals(ValidationType.VALID, bulkEntriesViewModel.getValidationType());
  }

  @Test
  public void validationTypeShouldBeValidWhenOnlyHasNewLot() {
    // given
    LotMovementViewModel newLotMovementViewModel = LotMovementViewModel.builder()
        .lotNumber(lotNumber2)
        .lotSoh(lotSoh)
        .movementReason(movementReason)
        .documentNumber(documentNumber)
        .expiryDate(expiryDate)
        .quantity(quantity)
        .build();
    bulkEntriesViewModel.getNewLotMovementViewModelList().add(newLotMovementViewModel);
    bulkEntriesViewModel.existingLotMovementViewModelList = new ArrayList<>();

    // when
    bulkEntriesViewModel.validate();

    // then
    assertEquals(ValidationType.VALID, bulkEntriesViewModel.getValidationType());
  }


}