package org.openlmis.core.utils.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.core.model.Implementation;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.Treatment;
import org.openlmis.core.view.viewmodel.malaria.ImplementationReportType;
import org.openlmis.core.view.viewmodel.malaria.ImplementationReportViewModel;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.make;
import static com.natpryce.makeiteasy.MakeItEasy.with;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.openlmis.core.helpers.ImplementationBuilder.executor;
import static org.openlmis.core.helpers.ImplementationBuilder.randomImplementation;
import static org.openlmis.core.helpers.ImplementationBuilder.treatments;
import static org.openlmis.core.helpers.ProductBuilder.code;
import static org.openlmis.core.helpers.ProductBuilder.randomProduct;
import static org.openlmis.core.helpers.TreatmentBuilder.product;
import static org.openlmis.core.helpers.TreatmentBuilder.randomTreatment;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

@RunWith(MockitoJUnitRunner.class)
public class ImplementationListToImplementationReportViewModelMapperTest {

    private static final String APE_EXECUTOR = "APE";
    private static final String US_EXECUTOR = "US";
    private static final String PRODUCT_6x1_CODE = "08O05";
    private static final String PRODUCT_6x2_CODE = PRODUCT_6x1_CODE + "Z";
    private static final String PRODUCT_6x3_CODE = PRODUCT_6x1_CODE + "Y";
    private static final String PRODUCT_6x4_CODE = PRODUCT_6x1_CODE + "X";

    @InjectMocks
    private ImplementationListToImplementationReportViewModelMapper mapper;
    private Treatment treatment;

    @Test
    public void shouldMap6x1ProductForUsImplementations() throws Exception {
        assertProductForImplementationWasMapped(PRODUCT_6x1_CODE, US_EXECUTOR, "6x1");
    }

    @Test
    public void shouldMap6x2ProductForUsImplementations() throws Exception {
        assertProductForImplementationWasMapped(PRODUCT_6x2_CODE, US_EXECUTOR, "6x2");
    }

    @Test
    public void shouldMap6x3ProductForUsImplementations() throws Exception {
        assertProductForImplementationWasMapped(PRODUCT_6x3_CODE, US_EXECUTOR, "6x3");
    }

    @Test
    public void shouldMap6x4ProductForUsImplementations() throws Exception {
        assertProductForImplementationWasMapped(PRODUCT_6x4_CODE, US_EXECUTOR, "6x4");
    }

    @Test
    public void shouldMap6x1ProductForApeImplementations() throws Exception {
        assertProductForImplementationWasMapped(PRODUCT_6x1_CODE, APE_EXECUTOR, "6x1");
    }

    @Test
    public void shouldMap6x2ProductForApeImplementations() throws Exception {
        assertProductForImplementationWasMapped(PRODUCT_6x2_CODE, APE_EXECUTOR, "6x2");
    }

    @Test
    public void shouldMap6x3ProductForApeImplementations() throws Exception {
        assertProductForImplementationWasMapped(PRODUCT_6x3_CODE, APE_EXECUTOR, "6x3");
    }

    @Test
    public void shouldMap6x4ProductForApeImplementations() throws Exception {
        assertProductForImplementationWasMapped(PRODUCT_6x4_CODE, APE_EXECUTOR, "6x4");
    }

    @Test
    public void shouldMapUsType() throws Exception {
        Implementation implementation = getImplementationForExecutor(PRODUCT_6x1_CODE, US_EXECUTOR);
        ImplementationReportViewModel viewModel = mapper.mapUsImplementations(newArrayList(implementation));
        assertThat(viewModel.getType(), is(ImplementationReportType.US));
    }

    @Test
    public void shouldMapApeType() throws Exception {
        Implementation implementation = getImplementationForExecutor(PRODUCT_6x1_CODE, APE_EXECUTOR);
        ImplementationReportViewModel viewModel = mapper.mapApeImplementations(newArrayList(implementation));
        assertThat(viewModel.getType(), is(ImplementationReportType.APE));
    }

    @Test
    public void shouldReturnEmptyIfImplementationsAreNoUsExecutor() throws Exception {
        Implementation implementation = make(a(randomImplementation));
        ImplementationReportViewModel implementationReportViewModel = mapper.mapUsImplementations(newArrayList
                (implementation));
        assertThat(implementationReportViewModel.getCurrentTreatment6x1(), is(0L));
        assertThat(implementationReportViewModel.getExistingStock6x1(), is(0L));
        assertThat(implementationReportViewModel.getCurrentTreatment6x2(), is(0L));
        assertThat(implementationReportViewModel.getExistingStock6x2(), is(0L));
        assertThat(implementationReportViewModel.getCurrentTreatment6x3(), is(0L));
        assertThat(implementationReportViewModel.getExistingStock6x3(), is(0L));
        assertThat(implementationReportViewModel.getCurrentTreatment6x4(), is(0L));
        assertThat(implementationReportViewModel.getExistingStock6x4(), is(0L));
    }

    private void assertProductForImplementationWasMapped(String productCode, String executorName, String productName) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Implementation implementation = getImplementationForExecutor(productCode, executorName);
        ImplementationReportViewModel implementationReportViewModel = executorName == US_EXECUTOR ?
                mapper.mapUsImplementations(newArrayList(implementation)) :
                mapper.mapApeImplementations(newArrayList(implementation));
        Method getCurrentTreatment = implementationReportViewModel.getClass().getMethod("getCurrentTreatment" + productName);
        Method getExistingStock = implementationReportViewModel.getClass().getMethod("getExistingStock" + productName);
        assertThat((long)getCurrentTreatment.invoke(implementationReportViewModel), is(treatment.getAmount()));
        assertThat((long)getExistingStock.invoke(implementationReportViewModel), is(treatment.getStock()));
    }

    private Implementation getImplementationForExecutor(String productCode, String executorName) {
        Product productWithCode = make(a(randomProduct, with(code, productCode)));
        treatment = make(a(randomTreatment, with(product, productWithCode)));
        return make(a(randomImplementation, with(treatments, newArrayList(treatment)),
                with(executor, executorName)));
    }
}