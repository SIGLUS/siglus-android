package org.openlmis.core.presenter;

import static org.assertj.core.util.Lists.newArrayList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Application;
import androidx.test.core.app.ApplicationProvider;
import com.google.inject.AbstractModule;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.Pod;
import org.openlmis.core.model.PodProductItem;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnRForm.Emergency;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.builder.PodBuilder;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.repository.PodRepository;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.RegimenRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.viewmodel.RegimeProductViewModel;
import roboguice.RoboGuice;
import rx.observers.TestSubscriber;

@RunWith(LMISTestRunner.class)
@SuppressWarnings("PMD")
public class ProductPresenterTest {

  private ProductPresenter presenter;
  private ProductRepository productRepository;
  private ProgramRepository programRepository;
  private RegimenRepository regimenRepository;
  private StockRepository stockRepository;
  private RnrFormRepository rnrFormRepository;
  private PodRepository podRepository;

  @Before
  public void setup() throws Exception {
    productRepository = mock(ProductRepository.class);
    programRepository = mock(ProgramRepository.class);
    stockRepository = mock(StockRepository.class);
    regimenRepository = mock(RegimenRepository.class);
    rnrFormRepository = mock(RnrFormRepository.class);
    podRepository = mock(PodRepository.class);

    Application application = ApplicationProvider.getApplicationContext();
    RoboGuice.overrideApplicationInjector(application, new AbstractModule() {
      @Override
      protected void configure() {
        bind(ProductRepository.class).toInstance(productRepository);
        bind(ProgramRepository.class).toInstance(programRepository);
        bind(RegimenRepository.class).toInstance(regimenRepository);
        bind(StockRepository.class).toInstance(stockRepository);
        bind(RnrFormRepository.class).toInstance(rnrFormRepository);
        bind(PodRepository.class).toInstance(podRepository);
      }
    });
    presenter = RoboGuice.getInjector(application)
        .getInstance(ProductPresenter.class);
  }

  @Test
  public void shouldLoadRegimeProducts() throws Exception {
    // when
    Regimen regimen = new Regimen();
    regimen.setCode("code");
    regimen.setName("3TC 150mg");
    when(regimenRepository.listNonCustomRegimen(anyString(), any())).thenReturn(newArrayList(regimen));
    Product product = new Product();
    product.setPrimaryName("PrimaryName");
    when(productRepository.getByCode("code")).thenReturn(product);
    TestSubscriber<List<RegimeProductViewModel>> subscriber = new TestSubscriber<>();
    presenter.loadRegimeProducts(Constants.VIA_PROGRAM_CODE, Regimen.RegimeType.Adults).subscribe(subscriber);

    subscriber.awaitTerminalEvent();

    subscriber.assertNoErrors();

    assertThat(subscriber.getOnNextEvents().get(0).size(), is(1));
    assertThat(subscriber.getOnNextEvents().get(0).get(0).getShortCode(), is("3TC 150mg"));
  }

  @Test
  public void loadEmergencyProducts() throws Exception {
    // given
    StockCard stockCard = new StockCard();
    stockCard
        .setProduct(new ProductBuilder().setPrimaryName("Product name").setCode("011111").build());
    when(stockRepository.listEmergencyStockCards()).thenReturn(newArrayList(stockCard));
    when(rnrFormRepository.listInclude(Emergency.NO, "VC")).thenReturn(newArrayList());
    // when
    TestSubscriber<List<InventoryViewModel>> subscriber = new TestSubscriber<>();
    presenter.loadEmergencyProducts().subscribe(subscriber);
    subscriber.awaitTerminalEvent();
    // then
    subscriber.assertNoErrors();

    assertThat(subscriber.getOnNextEvents().get(0).size(), is(1));
    assertThat(subscriber.getOnNextEvents().get(0).get(0).getProductName(), is("Product name"));
  }

  @Test
  public void shouldFilterInvalidProductId_whenCalledLoadEmergencyProductsAndShippedQuantityIsLessThanOrderQuantity()
      throws Exception {
    // given
    StockCard stockCard = new StockCard();
    Product product = new ProductBuilder().setPrimaryName("Product name").setCode("011111").build();

    stockCard.setProduct(product);
    when(stockRepository.listEmergencyStockCards()).thenReturn(newArrayList(stockCard));

    String programCode = "VC";
    Date periodBegin = DateUtil.getCurrentDate();
    RnRForm rnRForm = new RnRForm();
    rnRForm.setPeriodBegin(periodBegin);
    rnRForm.setEmergency(false);
    when(rnrFormRepository.listInclude(Emergency.NO, programCode))
        .thenReturn(newArrayList(rnRForm));

    PodProductItem mockedPodProductItem = mock(PodProductItem.class);
    when(mockedPodProductItem.getProduct()).thenReturn(product);
    when(mockedPodProductItem.getSumShippedQuantity()).thenReturn(5L);
    when(mockedPodProductItem.getOrderedQuantity()).thenReturn(10L);

    Pod pod = PodBuilder.generatePod();
    pod.setPodProductItemsWrapper(newArrayList(mockedPodProductItem));
    when(podRepository.queryRegularRemotePodsByProgramCodeAndPeriod(
            programCode, DateUtil.getFirstDayForCurrentMonthByDate(periodBegin)
        )
    ).thenReturn(newArrayList(pod));
    // when
    TestSubscriber<List<InventoryViewModel>> subscriber = new TestSubscriber<>();
    presenter.loadEmergencyProducts().subscribe(subscriber);
    subscriber.awaitTerminalEvent();
    // then
    assertEquals(0, subscriber.getOnNextEvents().get(0).size());
  }

  @Test
  public void shouldSaveRegime() throws Exception {
    when(regimenRepository.getByNameAndCategory(anyString(), any(Regimen.RegimeType.class)))
        .thenReturn(null);

    TestSubscriber<Regimen> subscriber = new TestSubscriber<>();
    presenter.saveRegimes(getInventoryViewModels(), Regimen.RegimeType.Adults)
        .subscribe(subscriber);

    subscriber.awaitTerminalEvent();

    subscriber.assertNoErrors();

    ArgumentCaptor<Regimen> regimenArgumentCaptor = ArgumentCaptor.forClass(Regimen.class);
    verify(regimenRepository).create(regimenArgumentCaptor.capture());

    Regimen regimen = regimenArgumentCaptor.getValue();
    assertThat(regimen.getName(), is("3TC 150mg"));
    assertThat(regimen.getType(), is(Regimen.RegimeType.Adults));
    assertThat(regimen.isCustom(), is(true));
  }

  @Test
  public void shouldNotSaveRegimeWhenHasRegimeInDB() throws Exception {
    when(regimenRepository.getByNameAndCategory(anyString(), any(Regimen.RegimeType.class)))
        .thenReturn(new Regimen());

    TestSubscriber<Regimen> subscriber = new TestSubscriber<>();
    presenter.saveRegimes(getInventoryViewModels(), Regimen.RegimeType.Adults)
        .subscribe(subscriber);

    subscriber.awaitTerminalEvent();

    subscriber.assertNoErrors();

    verify(regimenRepository, never()).create(any(Regimen.class));
  }

  private RegimeProductViewModel getInventoryViewModels() {
    return new RegimeProductViewModel("3TC 150mg");
  }
}