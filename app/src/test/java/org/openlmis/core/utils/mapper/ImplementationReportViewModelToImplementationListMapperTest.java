package org.openlmis.core.utils.mapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.helpers.ProductBuilder;
import org.openlmis.core.model.Implementation;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.Treatment;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.view.viewmodel.malaria.ImplementationReportType;
import org.openlmis.core.view.viewmodel.malaria.ImplementationReportViewModel;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.make;
import static com.natpryce.makeiteasy.MakeItEasy.with;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.openlmis.core.helpers.ImplementationBuilder.createDefaultImplementations;
import static org.openlmis.core.helpers.ImplementationReportBuilder.randomImplementationReport;
import static org.openlmis.core.helpers.ImplementationReportBuilder.type;
import static org.openlmis.core.utils.MalariaExecutors.APE;
import static org.openlmis.core.utils.MalariaExecutors.US;
import static org.openlmis.core.utils.MalariaProductCodes.PRODUCT_6x1_CODE;
import static org.openlmis.core.utils.MalariaProductCodes.PRODUCT_6x2_CODE;
import static org.openlmis.core.utils.MalariaProductCodes.PRODUCT_6x3_CODE;
import static org.openlmis.core.utils.MalariaProductCodes.PRODUCT_6x4_CODE;
import static org.openlmis.core.utils.mapper.ImplementationUtils.findImplementationForExecutor;
import static org.openlmis.core.utils.mapper.ImplementationUtils.findTreatmentForProduct;

@RunWith(MockitoJUnitRunner.class)
public class ImplementationReportViewModelToImplementationListMapperTest {

    private ImplementationReportViewModel usReport = make(a(randomImplementationReport, with(type, ImplementationReportType.US)));
    private ImplementationReportViewModel apeReport = make(a(randomImplementationReport, with(type, ImplementationReportType.APE)));
    private List<Implementation> implementations = createDefaultImplementations();

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ImplementationReportViewModelToImplementationListMapper mapper;
    private Product product6x1 = make(a(ProductBuilder.product6x1));
    private Product product6x2 = make(a(ProductBuilder.product6x2));
    private Product product6x3 = make(a(ProductBuilder.product6x3));
    private Product product6x4 = make(a(ProductBuilder.product6x4));

    @Before
    public void setUp() throws Exception {
        when(productRepository.getByCode(PRODUCT_6x1_CODE.getValue())).thenReturn(product6x1);
        when(productRepository.getByCode(PRODUCT_6x2_CODE.getValue())).thenReturn(product6x2);
        when(productRepository.getByCode(PRODUCT_6x3_CODE.getValue())).thenReturn(product6x3);
        when(productRepository.getByCode(PRODUCT_6x4_CODE.getValue())).thenReturn(product6x4);
    }

    @Test
    public void shouldMapProduct6x1ForUsReport() throws Exception {
        assertProductWasMapped(US.name(), "6x1", PRODUCT_6x1_CODE.getValue(), usReport);
    }

    @Test
    public void shouldMapProduct6x2ForUsReport() throws Exception {
        assertProductWasMapped(US.name(), "6x2", PRODUCT_6x2_CODE.getValue(), usReport);
    }

    @Test
    public void shouldMapProduct6x3ForUsReport() throws Exception {
        assertProductWasMapped(US.name(), "6x3", PRODUCT_6x3_CODE.getValue(), usReport);
    }

    @Test
    public void shouldMapProduct6x4ForUsReport() throws Exception {
        assertProductWasMapped(US.name(), "6x4", PRODUCT_6x4_CODE.getValue(), usReport);
    }

    @Test
    public void shouldMapProduct6x1ForApeReport() throws Exception {
        assertProductWasMapped(APE.name(), "6x1", PRODUCT_6x1_CODE.getValue(), apeReport);
    }

    @Test
    public void shouldMapProduct6x2ForApeReport() throws Exception {
        assertProductWasMapped(APE.name(), "6x2", PRODUCT_6x2_CODE.getValue(), apeReport);
    }

    @Test
    public void shouldMapProduct6x3ForApeReport() throws Exception {
        assertProductWasMapped(APE.name(), "6x3", PRODUCT_6x3_CODE.getValue(), apeReport);
    }

    @Test
    public void shouldMapProduct6x4ForApeReport() throws Exception {
        assertProductWasMapped(APE.name(), "6x4", PRODUCT_6x4_CODE.getValue(), apeReport);
    }

    private void assertProductWasMapped(String executor, String productPartialCode, String productCode, ImplementationReportViewModel report) throws LMISException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        mapper.map(report, implementations);
        Implementation implementation = findImplementationForExecutor(implementations, executor);
        Treatment treatment = findTreatmentForProduct(implementation.getTreatments(), productCode);
        Method getCurrentTreatment = ImplementationReportViewModel.class.getMethod("getCurrentTreatment" + productPartialCode);
        Method getExistingStock = ImplementationReportViewModel.class.getMethod("getExistingStock" + productPartialCode);
        Field productField = this.getClass().getDeclaredField("product" + productPartialCode);
        productField.setAccessible(true);
        assertThat(treatment, is(notNullValue()));
        assertThat(treatment.getAmount(), is(getCurrentTreatment.invoke(report)));
        assertThat(treatment.getStock(), is(getExistingStock.invoke(report)));
        assertThat(treatment.getProduct(), is((Product) productField.get(this)));
    }
}