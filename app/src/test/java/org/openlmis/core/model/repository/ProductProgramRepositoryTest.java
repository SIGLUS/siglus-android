package org.openlmis.core.model.repository;

import com.google.inject.AbstractModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.ProductProgram;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.builder.ProductProgramBuilder;
import org.robolectric.RuntimeEnvironment;

import java.util.Arrays;
import java.util.List;

import roboguice.RoboGuice;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.assertj.core.util.Lists.newArrayList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class ProductProgramRepositoryTest extends LMISRepositoryUnitTest {

    private ProductProgramRepository repository;
    private ProductRepository productRepository;

    @Before
    public void setUp() throws Exception {
        productRepository = mock(ProductRepository.class);

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
            @Override
            protected void configure() {
                bind(ProductRepository.class).toInstance(productRepository);
            }
        });

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

    @Test
    public void shouldListActiveProductProgramsByProgramCode() throws Exception {
        List<ProductProgram> productPrograms = Arrays.asList(
                new ProductProgramBuilder().setProgramCode("PR1").setProductCode("P1").setActive(true).build(),
                new ProductProgramBuilder().setProgramCode("PR2").setProductCode("P1").setActive(false).build()
        );
        repository.batchSave(productPrograms);

        List<ProductProgram> queriedProductPrograms = repository.listActiveProductProgramsByProgramCodes(Arrays.asList("PR1", "PR2"));

        assertEquals(1, queriedProductPrograms.size());
        assertEquals("PR1", queriedProductPrograms.get(0).getProgramCode());
    }


    @Test
    public void shouldQueryActiveProductIdsByProgramsWithKits() throws Exception {
        List<ProductProgram> productPrograms = Arrays.asList(
                new ProductProgramBuilder().setProgramCode("PR1").setProductCode("P1").setActive(true).build(),
                new ProductProgramBuilder().setProgramCode("PR1").setProductCode("P2").setActive(false).build(),
                new ProductProgramBuilder().setProgramCode("PR2").setProductCode("P1").setActive(true).build()
        );
        repository.batchSave(productPrograms);

        when(productRepository.queryActiveProductsByCodesWithKits(anyList(), anyBoolean())).thenReturn(newArrayList(
                new ProductBuilder().setCode("P1").setProductId(100L).build()
        ));

        List<Long> productIds = repository.queryActiveProductIdsByProgramsWithKits(newArrayList("PR1"), false);

        assertThat(productIds.size(), is(1));
        assertThat(productIds.get(0), is(100L));
    }
}