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

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;

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
    public void shouldSaveProgramWithProductsSuccessful() throws LMISException{
        Program program = new Program();

        program.setProducts(products);
        program.setProgramCode("TB");
        program.setProgramName("TB");

        programRepository.saveProgramWithProduct(program);

        assertThat(programRepository.list().size(), is(1));
        assertThat(programRepository.list().get(0).getProducts().size(), is(1));
    }
}
