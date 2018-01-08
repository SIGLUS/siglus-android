package org.openlmis.core.utils.mapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.Implementation;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.Treatment;
import org.openlmis.core.view.viewmodel.malaria.ImplementationReportViewModel;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

@RunWith(LMISTestRunner.class)
public class ImplementationUtilsTest {
    private Implementation implementation;
    private List<Implementation> implementations;
    private ArrayList<Treatment> treatments;
    private String productCode;
    private String US;
    private String emptyString;
    private String productName;
    private int amount;
    private int stock;

    @Before
    public void init() {
        emptyString = "";
        US = "US";
        implementation = new Implementation(US, null);
        Product product = new Product();
        productName = "6x1";
        productCode = "08O05";
        product.setCode(productCode);
        Treatment treatment = new Treatment();
        treatment.setProduct(product);
        amount = 1 + new Random().nextInt(9);
        stock = 1 + new Random().nextInt(9);
        treatment.setAmount(amount);
        treatment.setStock(stock);
        treatments = newArrayList(treatment);
        implementation.setTreatments(treatments);
        implementations = newArrayList(implementation);
    }

    @Test
    public void shouldReturnNullWhenImplementationIsNotFoundByExecutor() {
        Implementation implementationForExecutor = ImplementationUtils.findImplementationForExecutor(implementations, emptyString);
        assertThat(implementationForExecutor, is(nullValue()));
    }

    @Test
    public void shouldFindImplementationByExecutor() {
        Implementation implementationForExecutor = ImplementationUtils.findImplementationForExecutor(implementations, US);
        assertThat(implementationForExecutor.getExecutor(), is(implementation.getExecutor()));
    }

    @Test
    public void shouldFindTreatmentByProductCode(){
        Treatment treatmentForProduct = ImplementationUtils.findTreatmentForProduct(treatments, productCode);
        assertThat(treatmentForProduct.getProduct().getCode() ,is(productCode));
    }

    @Test
    public void shouldReturnNullWhenTreatmentIsNotFoundByProductCode(){
        Treatment treatmentForProduct = ImplementationUtils.findTreatmentForProduct(treatments, emptyString);
        assertThat(treatmentForProduct ,is(nullValue()));
    }

    @Test
    public void shouldUpdateValuesForViewModel() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        ImplementationReportViewModel implementationReportViewModel = new ImplementationReportViewModel();
        ImplementationUtils.mapForProduct(implementation, productName, implementationReportViewModel);
        assertThat(implementationReportViewModel.getCurrentTreatment6x1(), is((long) amount));
        assertThat(implementationReportViewModel.getExistingStock6x1(), is((long) stock));
    }

}