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
import org.openlmis.core.model.ReportTypeForm;
import org.openlmis.core.model.builder.ProgramBuilder;
import org.openlmis.core.model.builder.ReportTypeFormBuilder;
import org.openlmis.core.utils.DateUtil;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
@SuppressWarnings("PMD")
public class ProgramRepositoryTest extends LMISRepositoryUnitTest {

  ProgramRepository programRepository;
  ProductRepository productRepository;
  ReportTypeFormRepository reportTypeFormRepository;
  ArrayList<Product> products = new ArrayList<>();

  @Before
  public void setup() throws LMISException {
    programRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(ProgramRepository.class);
    productRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(ProductRepository.class);
    reportTypeFormRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(ReportTypeFormRepository.class);

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
    assertThat(productRepository.listActiveProducts(IsKit.NO).size(), is(1));

    //when add product to existing program
    Product newProduct = new Product();
    newProduct.setCode("new product code");
    newProduct.setPrimaryName("Test Product2");
    newProduct.setActive(true);
    products.add(newProduct);
    programRepository.createOrUpdateProgramWithProduct(programs);

    //then
    assertThat(programRepository.list().size(), is(size + 1)); //  1 new TB
    assertThat(productRepository.listActiveProducts(IsKit.NO).size(), is(2));
    assertThat(productRepository.listActiveProducts(IsKit.NO).get(1).getPrimaryName(),
        is("Test Product2"));

    //when createOrUpdateWithItems existing product
    newProduct.setPrimaryName("Test Product2 Updated");
    programRepository.createOrUpdateProgramWithProduct(programs);

    //then
    assertThat(programRepository.list().size(), is(size + 1)); //  1 new TB
    assertThat(productRepository.listActiveProducts(IsKit.NO).size(), is(2));
    assertThat(productRepository.listActiveProducts(IsKit.NO).get(1).getPrimaryName(),
        is("Test Product2 Updated"));
  }

  @Test
  public void shouldQueryActivePrograms() throws Exception {
    // given
    insertProgram("MMIA", "MMIA Program", null);
    insertProgram("VIA", "VIA Program", null);
    insertReportType("MMIA", "MMIA Program", true);
    insertReportType("PTV", "VIA Program", false);

    // when
    final List<Program> activePrograms = programRepository.queryActiveProgram();

    // then
    Assertions.assertThat(activePrograms.size()).isEqualTo(1);
    Assertions.assertThat(activePrograms.get(0).getProgramCode()).isEqualTo("MMIA");
  }

  private void insertProgram(String programCode, String programName, String parentCode)
      throws LMISException {
    Program program = new ProgramBuilder()
        .setProgramCode(programCode)
        .setProgramName(programName)
        .setParentCode(parentCode).build();
    programRepository.createOrUpdate(program);
  }

  private void insertReportType(String code, String programName, boolean isActive) throws LMISException {
    final ReportTypeForm reportTypeForm = new ReportTypeFormBuilder()
        .setCode(code)
        .setActive(isActive)
        .setName(programName)
        .setStartTime(DateUtil.getCurrentDate())
        .setLastReportEndTime("0")
        .build();
    reportTypeFormRepository.createOrUpdate(reportTypeForm);
  }
}
