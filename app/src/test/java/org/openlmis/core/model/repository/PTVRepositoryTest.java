package org.openlmis.core.model.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

import com.google.inject.AbstractModule;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.ProductProgram;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.Service;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.model.helper.FormHelper;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class PTVRepositoryTest extends LMISRepositoryUnitTest {

  PTVRepository ptvRepository;

  private ProgramRepository mockProgramRepository;
  private ProductRepository mockProductRepository;
  private RegimenRepository mockRegimenRepository;
  private ServiceFormRepository mockServiceFormRepository;
  private ProductProgramRepository mockProductProgramRepository;
  private FormHelper mockFormHelper;

  @Before
  public void setup() throws LMISException {
    mockProgramRepository = mock(ProgramRepository.class);
    mockProductRepository = mock(ProductRepository.class);
    mockRegimenRepository = mock(RegimenRepository.class);
    mockServiceFormRepository = mock(ServiceFormRepository.class);
    mockProductProgramRepository = mock(ProductProgramRepository.class);
    mockFormHelper = mock(FormHelper.class);

    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
      @Override
      protected void configure() {
        bind(ProgramRepository.class).toInstance(mockProgramRepository);
        bind(ProductRepository.class).toInstance(mockProductRepository);
        bind(RegimenRepository.class).toInstance(mockRegimenRepository);
        bind(ServiceFormRepository.class).toInstance(mockServiceFormRepository);
        bind(ProductProgramRepository.class).toInstance(mockProductProgramRepository);
        bind(FormHelper.class).toInstance(mockFormHelper);
      }
    });

    ptvRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(PTVRepository.class);
  }

  @Test
  public void shouldGetStockCardRnr() throws LMISException {
    ptvRepository = spy(ptvRepository);

    ArrayList<StockCard> stockCards = new ArrayList<>();
    Product product = new Product();
    product.setCode("01A01");
    product.setId(1234L);
    StockCard stockCard = new StockCardBuilder()
        .setCreateDate(new Date())
        .setProduct(product)
        .build();
    stockCard.setCreatedAt(DateUtil.parseString("10/10/2015", DateUtil.SIMPLE_DATE_FORMAT));
    stockCards.add(stockCard);

    RnRForm form = new RnRForm();
    form.setPeriodBegin(DateUtil.parseString("9/21/2015", DateUtil.SIMPLE_DATE_FORMAT));
    form.setPeriodEnd(DateUtil.parseString("10/20/2015", DateUtil.SIMPLE_DATE_FORMAT));
    final Program program = new Program(Constants.PTV_PROGRAM_CODE, Constants.PTV_PROGRAM_CODE,
        null, false, null, null);
    form.setProgram(program);
    List<String> mockProgramCode = new ArrayList<>();
    mockProgramCode.add(program.getProgramCode());
    ProductProgram productProgram = new ProductProgram();
    productProgram.setCategory("Adult");
    List<Long> mockProductIds = new ArrayList<>();
    mockProductIds.add(product.getId());
    List<Product> products = new ArrayList<>();
    products.add(product);

    when(mockProductProgramRepository
        .queryActiveProductIdsByProgramsWithKits(anyListOf(String.class), anyBoolean()))
        .thenReturn(mockProductIds);
    when(mockProductProgramRepository.queryByCode(anyString(), anyString()))
        .thenReturn(productProgram);
    when(mockProductRepository.queryProductsByProductIds(anyListOf(Long.class)))
        .thenReturn(products);
    when(mockProgramRepository.queryByCode(anyString())).thenReturn(program);
    when(mockServiceFormRepository.listAllActiveWithProgram(any())).thenReturn(newArrayList());
    doReturn(newArrayList(form)).when(ptvRepository)
        .listInclude(any(RnRForm.Emergency.class), anyString());

    List<RnrFormItem> rnrFormItems = ptvRepository.generateRnrFormItems(form, stockCards);

    assertThat(rnrFormItems.get(0).getProduct(), is(product));

    final Service service = new Service();
    when(mockServiceFormRepository.listAllActiveWithProgram(any()))
        .thenReturn(newArrayList(service));

    rnrFormItems = ptvRepository.generateRnrFormItems(form, stockCards);

    assertThat(rnrFormItems.get(0).getServiceItemListWrapper().get(0).getService(), is(service));
  }
}
