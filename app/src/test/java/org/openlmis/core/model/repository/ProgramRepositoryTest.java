package org.openlmis.core.model.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.Program;
import org.robolectric.Robolectric;

import java.util.ArrayList;

import roboguice.RoboGuice;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(LMISTestRunner.class)
public class ProgramRepositoryTest extends LMISRepositoryUnitTest {
    ProgramRepository programRepository;
    ProductRepository productRepository;
    ArrayList<Product> products = new ArrayList<>();

    @Before
    public void setup() throws LMISException {
        programRepository = RoboGuice.getInjector(Robolectric.application).getInstance(ProgramRepository.class);
        productRepository = RoboGuice.getInjector(Robolectric.application).getInstance(ProductRepository.class);

        Product product = new Product();
        product.setPrimaryName("Test Product");
        product.setStrength("200");

        productRepository.create(product);

        products.add(product);
    }


    @Test
    public void shouldSaveProgramWithProductsSuccessful() throws LMISException {
        Program program = new Program();

        program.setProducts(products);
        program.setProgramCode("TB");
        program.setProgramName("TB");

        programRepository.saveProgramWithProduct(program);

        assertThat(programRepository.list().size(), is(1));
        assertThat(programRepository.list().get(0).getProducts().size(), is(1));
    }

    @Test
    public void shouldSetMMIAProductMedicineTypeAndSortCorrectly() throws Exception {
        ArrayList<Product> list = new ArrayList<>();

        list.add(getProduct(1L, "product", "08S17", Product.MEDICINE_TYPE_OTHER));
        list.add(getProduct(2L, "product2", "08S32B", Product.MEDICINE_TYPE_BABY));
        list.add(getProduct(3L, "product3", "08S39Z", Product.MEDICINE_TYPE_ADULT));

        Program program = new Program();
        program.setProducts(list);
        program.setProgramCode(MMIARepository.MMIA_PROGRAM_CODE);

        programRepository.saveProgramWithProduct(program);


        ArrayList<Product> products = new ArrayList(program.getProducts());
        assertThat(products.get(0).getMedicine_type(), is(Product.MEDICINE_TYPE_ADULT));
        assertThat(products.get(2).getMedicine_type(), is(Product.MEDICINE_TYPE_OTHER));
    }

    private Product getProduct(long id, String primaryName, String code, String medicineType) {
        Product product = new Product();
        product.setId(id);
        product.setPrimaryName(primaryName);
        product.setCode(code);
        product.setMedicine_type(medicineType);
        return product;
    }

    @Test
    public void shouldSaveProgramWithProductsSuccessful1() throws LMISException {
        Program program = new Program();

        program.setProducts(products);
        program.setProgramCode("TB");
        program.setProgramName("TB");

        programRepository.saveProgramWithProduct(program);

        assertThat(programRepository.list().size(), is(1));
        assertThat(programRepository.list().get(0).getProducts().size(), is(1));
    }
}
