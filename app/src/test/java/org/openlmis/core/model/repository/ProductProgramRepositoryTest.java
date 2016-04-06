package org.openlmis.core.model.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.ProductProgram;
import org.openlmis.core.model.builder.ProductProgramBuilder;
import org.robolectric.RuntimeEnvironment;

import java.util.Arrays;
import java.util.List;

import roboguice.RoboGuice;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

@RunWith(LMISTestRunner.class)
public class ProductProgramRepositoryTest extends LMISRepositoryUnitTest {

    private ProductProgramRepository repository;

    @Before
    public void setUp() throws Exception {
        repository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(ProductProgramRepository.class);
    }

    @Test
    public void shouldBatchSaveAndQueryProductPrograms() throws Exception {
        List<ProductProgram> productPrograms = Arrays.asList(
                new ProductProgramBuilder().setProgramCode("PR1").setProductCode("P1").setActive(true).build(),
                new ProductProgramBuilder().setProgramCode("PR1").setProductCode("P2").setActive(false).build(),
                new ProductProgramBuilder().setProgramCode("PR2").setProductCode("P1").setActive(true).build()
        );

        repository.batchSave(productPrograms);

        ProductProgram productProgram = repository.queryByCode("P2", "PR1");
        assertFalse(productProgram.isActive());
    }

    @Test
    public void shouldNotCreateDuplicateProductProgram() throws Exception {
        List<ProductProgram> productPrograms = Arrays.asList(
                new ProductProgramBuilder().setProgramCode("PR1").setProductCode("P1").setActive(true).build(),
                new ProductProgramBuilder().setProgramCode("PR1").setProductCode("P1").setActive(false).build()
        );

        repository.batchSave(productPrograms);

        assertEquals(1, repository.listAll().size());

        ProductProgram productProgram = repository.queryByCode("P1", "PR1");
        assertFalse(productProgram.isActive());

    }
}