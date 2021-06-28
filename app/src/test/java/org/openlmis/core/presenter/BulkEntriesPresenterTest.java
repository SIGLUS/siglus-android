package org.openlmis.core.presenter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.inject.AbstractModule;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.DraftBulkEntriesProduct;
import org.openlmis.core.model.DraftBulkEntriesProductLotItem;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.repository.BulkEntriesRepository;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;


@RunWith(LMISTestRunner.class)
public class BulkEntriesPresenterTest {

  private BulkEntriesPresenter bulkEntriesPresenter;
  BulkEntriesRepository bulkEntriesRepository;
  private List<DraftBulkEntriesProduct> bulkEntriesViewModels = new ArrayList<>();

  @Before
  public void setup() throws Exception {
    bulkEntriesRepository = mock(BulkEntriesRepository.class);
    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
      @Override
      protected void configure() {
        bind(BulkEntriesRepository.class).toInstance(bulkEntriesRepository);
      }
    });
    bulkEntriesPresenter = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(BulkEntriesPresenter.class);


  }

  @Test
  public void shouldRestoreDraftBulkEntriesViewModel() throws LMISException {
    // given
    Product product = Product.builder()
        .isActive(true)
        .isArchived(false)
        .isBasic(true)
        .code("22A07")
        .isHiv(false)
        .isKit(false)
        .build();
    DraftBulkEntriesProductLotItem draftBulkEntriesProductLotItem = DraftBulkEntriesProductLotItem
        .builder()
        .lotNumber("yyy66")
        .lotSoh(Long.valueOf(100))
        .quantity(Long.valueOf(200))
        .reason("District( DDM)")
        .expirationDate(new Date("2023/07/13"))
        .newAdded(true)
        .build();

    List<DraftBulkEntriesProductLotItem> draftBulkEntriesProductLotItems = new ArrayList<>();
    draftBulkEntriesProductLotItems.add(draftBulkEntriesProductLotItem);

    DraftBulkEntriesProduct draftBulkEntriesProduct = DraftBulkEntriesProduct.builder()
        .product(product)
        .draftLotItemListWrapper(draftBulkEntriesProductLotItems)
        .quantity(Long.valueOf(300))
        .done(true)
        .build();
    bulkEntriesViewModels.add(draftBulkEntriesProduct);
    when(bulkEntriesRepository.queryAllBulkEntriesDraft()).thenReturn(bulkEntriesViewModels);
    // when
    bulkEntriesPresenter.restoreDraftInventory();
    // then
    assertEquals(bulkEntriesPresenter.getBulkEntriesViewModels().get(0).getQuantity(),Long.valueOf(300));
  }

}