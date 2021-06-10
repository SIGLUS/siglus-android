package org.openlmis.core.model.repository;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.Product.IsKit;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.builder.ProgramBuilder;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class ProgramRepositoryTest extends LMISRepositoryUnitTest {

  ProgramRepository programRepository;
  ProductRepository productRepository;
  ArrayList<Product> products = new ArrayList<>();

  @Before
  public void setup() throws LMISException {
    programRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(ProgramRepository.class);
    productRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(ProductRepository.class);

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
    int size = programRepository.list().size();
    //given
    Program program = new Program();
    program.setProgramCode("TB_TEST");
    program.setProgramName("TB_TEST");
    program.setProducts(products);

    ArrayList<Program> programs = new ArrayList<Program>();
    programs.add(program);

    //when add new program and products
    programRepository.createOrUpdateProgramWithProduct(programs);

    //then
    assertThat(programRepository.list().size(), is(size + 1)); // 1 new TB
    assertThat(productRepository.listActiveProducts(IsKit.No).size(), is(1));

    //when add product to existing program
    Product newProduct = new Product();
    newProduct.setCode("new product code");
    newProduct.setPrimaryName("Test Product2");
    newProduct.setActive(true);
    products.add(newProduct);
    programRepository.createOrUpdateProgramWithProduct(programs);

    //then
    assertThat(programRepository.list().size(), is(size + 1)); //  1 new TB
    assertThat(productRepository.listActiveProducts(IsKit.No).size(), is(2));
    assertThat(productRepository.listActiveProducts(IsKit.No).get(1).getPrimaryName(),
        is("Test Product2"));

    //when createOrUpdateWithItems existing product
    newProduct.setPrimaryName("Test Product2 Updated");
    programRepository.createOrUpdateProgramWithProduct(programs);

    //then
    assertThat(programRepository.list().size(), is(size + 1)); //  1 new TB
    assertThat(productRepository.listActiveProducts(IsKit.No).size(), is(2));
    assertThat(productRepository.listActiveProducts(IsKit.No).get(1).getPrimaryName(),
        is("Test Product2 Updated"));
  }

  @Test
  public void shouldQueryProgramIdsByProgramCodeOrParentCode() throws Exception {
    insertProgram("MMIA", "MMIA Program", null);
    insertProgram("PTV", "ptv Program", "MMIA");
    insertProgram("TARV", "tarv Program", "MMIA");
    insertProgram("VIA", "VIA Program", null);
    insertProgram("TB", "Nutrition Program", "VIA");

    List<Long> viaProgramIds = programRepository.queryProgramIdsByProgramCodeOrParentCode("VIA");
    List<Long> mmiaProgramIds = programRepository.queryProgramIdsByProgramCodeOrParentCode("MMIA");
    Assertions.assertThat(viaProgramIds.size())
        .isGreaterThanOrEqualTo(2);//initialized VIA programs + TB
    Assertions.assertThat(mmiaProgramIds.size()).isGreaterThanOrEqualTo(3); // TARV + PTV + MMIA
  }

  @Test
  public void shouldQueryProgramCodesByProgramCodeOrParentCode() throws Exception {
    insertProgram("MMIA", "MMIA Program", null);
    insertProgram("PTV", "ptv Program", "MMIA");
    insertProgram("TARV", "tarv Program", "MMIA");
    insertProgram("VIA", "VIA Program", null);
    insertProgram("TB", "Nutrition Program", "VIA");

    List<String> viaProgramCodes = programRepository
        .queryProgramCodesByProgramCodeOrParentCode("VIA");
    List<String> mmiaProgramCodes = programRepository
        .queryProgramCodesByProgramCodeOrParentCode("MMIA");
    assertTrue(viaProgramCodes.contains("TB"));
    Assertions.assertThat(mmiaProgramCodes.size()).isGreaterThanOrEqualTo(3);
    assertTrue(mmiaProgramCodes.contains("PTV") && mmiaProgramCodes.contains("MMIA"));
  }

  private void insertProgram(String programCode, String programName, String parentCode)
      throws LMISException {
    Program program = new ProgramBuilder()
        .setProgramCode(programCode)
        .setProgramName(programName)
        .setParentCode(parentCode).build();
    programRepository.createOrUpdate(program);
  }
}
