package org.openlmis.core.model;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.openlmis.core.utils.DateUtil.DB_DATE_FORMAT;

import androidx.annotation.NonNull;
import java.util.Date;
import org.junit.Test;
import org.openlmis.core.utils.DateUtil;

public class LotOnHandTest {

  @Test
  public void shouldReturnLotExcelModelWhenConvertToExcelModelIsCalled() {
    // given
    String productCode = "productCode";
    String productName = "primaryName";
    String lotNumber = "lotNumber";
    Date currentDate = DateUtil.getCurrentDate();
    String partialFulfilled = "partialFulfilled";
    String orderedQuantity = "ordered";
    String suppliedQuantity = "5";

    LotOnHand lotOnHand = createLotOnHand(productCode, productName, lotNumber, currentDate, "10");
    // when
    LotExcelModel lotExcelModel = lotOnHand.convertToExcelModel(orderedQuantity, partialFulfilled,
        suppliedQuantity);
    // then
    assertEquals(productCode, lotExcelModel.productCode);
    assertEquals(productName, lotExcelModel.productName);
    assertEquals(lotNumber, lotExcelModel.lotNumber);
    assertEquals(DateUtil.formatDate(currentDate, DB_DATE_FORMAT), lotExcelModel.expirationDate);
    assertEquals(partialFulfilled, lotExcelModel.partialFulfilled);
    assertEquals(orderedQuantity, lotExcelModel.orderedQuantity);
    assertEquals(suppliedQuantity, lotExcelModel.suppliedQuantity);
    assertEquals("10", lotExcelModel.price);
    assertEquals("50", lotExcelModel.totalValue);
  }

  @Test
  public void shouldReturnLotExcelModelWith0TotalValueWhenConvertToExcelModelIsCalledAndProductPriceIsNull() {
    // given
    Date currentDate = DateUtil.getCurrentDate();

    LotOnHand lotOnHand = createLotOnHand(
        "productCode", "primaryName", "lotNumber", currentDate, null
    );
    // when
    LotExcelModel lotExcelModel = lotOnHand.convertToExcelModel(
        "ordered", "partialFulfilled", "5"
    );
    // then
    assertEquals("0", lotExcelModel.totalValue);
  }

  @NonNull
  private LotOnHand createLotOnHand(
      String productCode,
      String productName,
      String lotNumber,
      Date currentDate,
      String price
  ) {
    Product product = new Product();
    product.code = productCode;
    product.primaryName = productName;
    product.price = price;

    Lot lot = new Lot();
    lot.setProduct(product);
    lot.lotNumber = lotNumber;
    lot.expirationDate = currentDate;

    return new LotOnHand(lot, mock(StockCard.class), 10L);
  }
}