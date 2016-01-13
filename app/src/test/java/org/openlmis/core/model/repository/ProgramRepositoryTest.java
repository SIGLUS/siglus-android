package org.openlmis.core.model.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.Product.IsKit;
import org.openlmis.core.model.Program;
import org.robolectric.RuntimeEnvironment;

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
        programRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(ProgramRepository.class);
        productRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(ProductRepository.class);

        Product product = new Product();
        product.setCode("test code");
        product.setPrimaryName("Test Product");
        product.setStrength("200");
        product.setActive(true);

        productRepository.createOrUpdate(product);

        products.add(product);
    }

    @Test
    public void shouldSaveProgramWithProductsSuccessful() throws LMISException {
        //given
        Program program = new Program();
        program.setProgramCode("TB");
        program.setProgramName("TB");
        program.setProducts(products);

        ArrayList<Program> programs = new ArrayList<Program>();
        programs.add(program);

        //when add new program and products
        programRepository.createOrUpdateProgramWithProduct(programs);

        //then
        assertThat(programRepository.list().size(), is(1));
        assertThat(productRepository.listActiveProducts(IsKit.No).size(), is(1));

        //when add product to existing program
        Product newProduct = new Product();
        newProduct.setCode("new product code");
        newProduct.setPrimaryName("Test Product2");
        newProduct.setActive(true);
        products.add(newProduct);
        programRepository.createOrUpdateProgramWithProduct(programs);

        //then
        assertThat(programRepository.list().size(), is(1));
        assertThat(productRepository.listActiveProducts(IsKit.No).size(), is(2));
        assertThat(productRepository.listActiveProducts(IsKit.No).get(1).getPrimaryName(), is("Test Product2"));

        //when update existing product
        newProduct.setPrimaryName("Test Product2 Updated");
        programRepository.createOrUpdateProgramWithProduct(programs);

        //then
        assertThat(programRepository.list().size(), is(1));
        assertThat(productRepository.listActiveProducts(IsKit.No).size(), is(2));
        assertThat(productRepository.listActiveProducts(IsKit.No).get(1).getPrimaryName(), is("Test Product2 Updated"));
    }

}
