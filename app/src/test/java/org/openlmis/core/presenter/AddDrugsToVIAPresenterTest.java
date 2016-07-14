package org.openlmis.core.presenter;

import com.google.inject.AbstractModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.repository.ProductProgramRepository;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.RnrFormItemRepository;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.robolectric.RuntimeEnvironment;

import java.util.List;

import roboguice.RoboGuice;
import rx.observers.TestSubscriber;

import static org.assertj.core.util.Lists.newArrayList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class AddDrugsToVIAPresenterTest {

    private AddDrugsToVIAPresenter presenter;
    private ProductRepository productRepository;
    private ProgramRepository programRepository;
    private ProductProgramRepository productProgramRepository;
    private RnrFormItemRepository rnrFormItemRepository;

    @Before
    public void setup() throws Exception {
        productRepository = mock(ProductRepository.class);
        programRepository = mock(ProgramRepository.class);
        productProgramRepository = mock(ProductProgramRepository.class);
        rnrFormItemRepository = mock(RnrFormItemRepository.class);

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
            @Override
            protected void configure() {
                bind(ProductRepository.class).toInstance(productRepository);
                bind(ProgramRepository.class).toInstance(programRepository);
                bind(ProductProgramRepository.class).toInstance(productProgramRepository);
                bind(RnrFormItemRepository.class).toInstance(rnrFormItemRepository);
            }
        });
        presenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(AddDrugsToVIAPresenter.class);
    }


    @Test
    public void loadActiveProductsNotInVIAForm() throws Exception {

        List<String> viaProgramCodes = newArrayList("PR1", "PR2");
        when(programRepository.queryProgramCodesByProgramCodeOrParentCode(Constants.VIA_PROGRAM_CODE)).thenReturn(newArrayList(viaProgramCodes));
        List<Long> productIdList = newArrayList(1L, 2L, 3L, 4L, 5L);
        when(productProgramRepository.queryActiveProductIdsByProgramsWithKits(viaProgramCodes, false)).thenReturn(productIdList);
        List<Long> productsNotInVia = newArrayList(1L, 2L, 3L);
        when(rnrFormItemRepository.listAllProductIdsInCurrentVIADraft()).thenReturn(productsNotInVia);

        when(productRepository.getById(4L)).thenReturn(new ProductBuilder().setCode("P1").setPrimaryName("ABC").build());
        when(productRepository.getById(5L)).thenReturn(new ProductBuilder().setCode("P2").setPrimaryName("DEF").build());

        TestSubscriber<List<InventoryViewModel>> subscriber = new TestSubscriber<>();
        presenter.loadActiveProductsNotInVIAForm().subscribe(subscriber);

        subscriber.awaitTerminalEvent();

        subscriber.assertNoErrors();

        assertThat(subscriber.getOnNextEvents().get(0).size(), is(2));
        assertThat(subscriber.getOnNextEvents().get(0).get(0).getProductName(), is("ABC"));
        assertThat(subscriber.getOnNextEvents().get(0).get(1).getProductName(), is("DEF"));
    }

}