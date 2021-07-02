package org.openlmis.core.network.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openlmis.core.network.adapter.StockCardsResponseAdapter.NetworkMovementType.mapToLocalMovementType;

import com.google.gson.JsonParser;
import com.google.inject.AbstractModule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager.MovementType;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.LotMovementItem;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.network.adapter.StockCardsResponseAdapter.NetworkMovementType;
import org.openlmis.core.network.model.StockCardsLocalResponse;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.JsonFileReader;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class StockCardsResponseAdapterTest {

  private final String productCode = "08A07";
  private StockCardsResponseAdapter adapter;
  private ProductRepository mockProductRepository;

  @Before
  public void setUp() throws Exception {
    mockProductRepository = mock(ProductRepository.class);
    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
    adapter = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(StockCardsResponseAdapter.class);
  }

  @Test
  public void shouldCorrectlySetProperty() throws Exception {
    // given
    final Product product = ProductBuilder.buildAdultProduct();
    product.setCode(productCode);
    when(mockProductRepository.getByCode(anyString())).thenReturn(product);
    String json = JsonFileReader.readJson(getClass(), "StockCardsResponse.json");

    // when
    StockCardsLocalResponse stockCardsLocalResponse = adapter
        .deserialize(new JsonParser().parse(json), null, null);
    final StockCard stockCard = stockCardsLocalResponse.getStockCards().get(0);
    assertNotNull(json);

    // then
    assertEquals(100, stockCard.getStockOnHand());
    assertEquals(productCode, stockCard.getProduct().getCode());

    final StockMovementItem stockMovementItem = stockCard.getStockMovementItemsWrapper().get(0);
    assertEquals(1, stockCard.getStockMovementItemsWrapper().size());
    assertEquals(200, stockMovementItem.getMovementQuantity());
    assertEquals(MovementType.POSITIVE_ADJUST, stockMovementItem.getMovementType());
    assertNull(stockMovementItem.getDocumentNumber());
    assertNull(stockMovementItem.getReason());
    assertEquals(300, stockMovementItem.getStockOnHand());
    assertEquals("hui", stockMovementItem.getSignature());
    assertEquals(100, stockMovementItem.getRequested().longValue());
    assertEquals("2021-06-21", DateUtil.formatDate(stockMovementItem.getCreatedTime(), DateUtil.DB_DATE_FORMAT));

    final LotMovementItem lotMovementItem = stockMovementItem.getLotMovementItemListWrapper().get(0);
    assertEquals(1, stockMovementItem.getLotMovementItemListWrapper().size());
    assertEquals("123", lotMovementItem.getDocumentNumber());
    assertEquals("CUSTOMER_RETURN", lotMovementItem.getReason());
    assertEquals("08A07-movement", lotMovementItem.getLot().getLotNumber());
    assertEquals(200, lotMovementItem.getMovementQuantity().longValue());
    assertEquals(300, lotMovementItem.getStockOnHand().longValue());

    final LotOnHand lotOnHand = stockCard.getLotOnHandListWrapper().get(0);
    assertEquals(1, stockCard.getLotOnHandListWrapper().size());
    assertEquals(300, lotOnHand.getQuantityOnHand().longValue());

    final Lot lot = lotOnHand.getLot();
    assertEquals(productCode, lot.getProduct().getCode());
    assertEquals("08A07-on-hand", lot.getLotNumber());
    assertEquals("2022-05-11", DateUtil.formatDate(lot.getExpirationDate(), DateUtil.DB_DATE_FORMAT));
  }

  @Test
  public void shouldCorrectlySetPropertyForKit() throws Exception {
    // given
    final Product product = ProductBuilder.buildAdultProduct();
    product.setCode(productCode);
    when(mockProductRepository.getByCode(anyString())).thenReturn(product);
    String json = JsonFileReader.readJson(getClass(), "StockCardsResponseForKit.json");

    // when
    StockCardsLocalResponse stockCardsLocalResponse = adapter
        .deserialize(new JsonParser().parse(json), null, null);
    final StockCard stockCard = stockCardsLocalResponse.getStockCards().get(0);
    assertNotNull(json);

    // then
    assertEquals(100, stockCard.getStockOnHand());
    assertEquals(productCode, stockCard.getProduct().getCode());

    final StockMovementItem stockMovementItem = stockCard.getStockMovementItemsWrapper().get(0);
    assertEquals("CUSTOMER_RETURN", stockMovementItem.getReason());
    assertEquals("123", stockMovementItem.getDocumentNumber());

    assertEquals(0, stockMovementItem.getLotMovementItemListWrapper().size());
    assertEquals(0, stockCard.getLotOnHandListWrapper().size());
  }

  @Test
  public void testMapToLocalMovementType() throws LMISException {
    // when
    final MovementType physicalInventory = mapToLocalMovementType(NetworkMovementType.PHYSICAL_INVENTORY.name(), 0);
    final MovementType positiveAdjustWithInventory = mapToLocalMovementType(
        NetworkMovementType.PHYSICAL_INVENTORY.name(), 100);
    final MovementType negativeAdjustWithInventory = mapToLocalMovementType(
        NetworkMovementType.PHYSICAL_INVENTORY.name(), -100);
    final MovementType receive = mapToLocalMovementType(NetworkMovementType.RECEIVE.name(), 100);
    final MovementType issueWithIssue = mapToLocalMovementType(NetworkMovementType.ISSUE.name(), -100);
    final MovementType issueWithUnpackKit = mapToLocalMovementType(NetworkMovementType.UNPACK_KIT.name(), -100);
    final MovementType positiveAdjustWithAdjustment = mapToLocalMovementType(NetworkMovementType.ADJUSTMENT.name(),
        100);
    final MovementType negativeAdjustWithAdjustment = mapToLocalMovementType(NetworkMovementType.ADJUSTMENT.name(),
        -100);
    final IllegalArgumentException adjustmentIllegalException = assertThrows(IllegalArgumentException.class,
        () -> mapToLocalMovementType(NetworkMovementType.ADJUSTMENT.name(), 0));
    final LMISException errorTypeException = assertThrows(LMISException.class,
        () -> mapToLocalMovementType("ERROR_TYPE", 0));

    // then
    assertEquals(MovementType.PHYSICAL_INVENTORY, physicalInventory);
    assertEquals(MovementType.POSITIVE_ADJUST, positiveAdjustWithInventory);
    assertEquals(MovementType.NEGATIVE_ADJUST, negativeAdjustWithInventory);
    assertEquals(MovementType.RECEIVE, receive);
    assertEquals(MovementType.ISSUE, issueWithIssue);
    assertEquals(MovementType.ISSUE, issueWithUnpackKit);
    assertEquals(MovementType.POSITIVE_ADJUST, positiveAdjustWithAdjustment);
    assertEquals(MovementType.NEGATIVE_ADJUST, negativeAdjustWithAdjustment);
    assertEquals("Adjustment quantity cannot be 0", adjustmentIllegalException.getMessage());
    assertEquals("Illegal network movement type: ERROR_TYPE", errorTypeException.getMsg());
  }

  @Test
  public void testMapToLocalReason() throws LMISException {
    // when
    final String unpackKitReason = adapter.mapToLocalReason(NetworkMovementType.UNPACK_KIT.name(), null);
    final String testReason = adapter.mapToLocalReason(NetworkMovementType.ADJUSTMENT.name(), "test");

    // then
    assertEquals("UNPACK_KIT", unpackKitReason);
    assertEquals("test", testReason);
  }

  @Test
  public void shouldConvertCorrectNetworkMovementType() throws LMISException {
    // given
    String physicalInventoryString = "PHYSICAL_INVENTORY";
    String receiveString = "RECEIVE";
    String issueString = "ISSUE";
    String adjustmentString = "ADJUSTMENT";
    String unpackKitString = "UNPACK_KIT";
    String errorString = "ERROR_TYPE";

    // when
    final NetworkMovementType physicalInventoryType = NetworkMovementType.convertValue(physicalInventoryString);
    final NetworkMovementType receiveType = NetworkMovementType.convertValue(receiveString);
    final NetworkMovementType issueType = NetworkMovementType.convertValue(issueString);
    final NetworkMovementType adjustmentType = NetworkMovementType.convertValue(adjustmentString);
    final NetworkMovementType unpackKitType = NetworkMovementType.convertValue(unpackKitString);
    final LMISException exception = Assert
        .assertThrows(LMISException.class, () -> NetworkMovementType.convertValue(errorString));

    // then
    assertEquals(NetworkMovementType.PHYSICAL_INVENTORY, physicalInventoryType);
    assertEquals(NetworkMovementType.RECEIVE, receiveType);
    assertEquals(NetworkMovementType.ISSUE, issueType);
    assertEquals(NetworkMovementType.ADJUSTMENT, adjustmentType);
    assertEquals(NetworkMovementType.UNPACK_KIT, unpackKitType);
    assertEquals("Illegal network movement type: " + errorString, exception.getMsg());
  }

  public class MyTestModule extends AbstractModule {

    @Override
    protected void configure() {
      bind(ProductRepository.class).toInstance(mockProductRepository);
    }
  }
}