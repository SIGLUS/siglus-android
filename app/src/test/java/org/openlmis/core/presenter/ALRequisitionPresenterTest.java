package org.openlmis.core.presenter;

import androidx.annotation.NonNull;

import com.google.inject.AbstractModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.builder.RnrFormItemBuilder;
import org.openlmis.core.model.repository.ALRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.view.viewmodel.ALGridViewModel;
import org.openlmis.core.view.viewmodel.ALReportItemViewModel;
import org.roboguice.shaded.goole.common.collect.Lists;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import roboguice.RoboGuice;
import rx.observers.TestSubscriber;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class ALRequisitionPresenterTest {

    private ALRequisitionPresenter alRequisitionPresenter;
    private ALRequisitionPresenter.ALRequisitionView alRequisitionView;
    private ALRepository mockALRepository;
    private RnrFormRepository mockRnrFormRepository;

    @Before
    public void setUp() throws ViewNotMatchException {
        alRequisitionPresenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(ALRequisitionPresenter.class);
        alRequisitionView = mock(ALRequisitionPresenter.ALRequisitionView.class);
        mockALRepository = mock(ALRepository.class);
        mockRnrFormRepository = mock(RnrFormRepository.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
        MockitoAnnotations.initMocks(this);
        alRequisitionPresenter.attachView(alRequisitionView);
    }

    @NonNull
    private RnRForm createRnrForm(RnRForm.Emergency emergency) {
        RnRForm rnRForm = new RnRForm();
        rnRForm.setEmergency(emergency.Emergency());
        return rnRForm;
    }

    @Test
    public void shouldALViewArchFullDisplayed() {
        alRequisitionPresenter.rnRForm = createRnrForm(RnRForm.Emergency.No);
        alRequisitionPresenter.updateFormUI();
        ALReportItemViewModel itemHF = alRequisitionPresenter.alReportViewModel.getItemHF();
        ALReportItemViewModel itemCHW = alRequisitionPresenter.alReportViewModel.getItemCHW();
        ALReportItemViewModel itemTotal = alRequisitionPresenter.alReportViewModel.getItemTotal();

        assertThat(itemHF.getGridOne().getColumnCode(), is(ALGridViewModel.ALColumnCode.OneColumn));
        assertThat(itemHF.getGridTwo().getColumnCode(), is(ALGridViewModel.ALColumnCode.TwoColumn));
        assertThat(itemHF.getGridThree().getColumnCode(), is(ALGridViewModel.ALColumnCode.ThreeColumn));
        assertThat(itemHF.getGridFour().getColumnCode(), is(ALGridViewModel.ALColumnCode.FourColumn));

        assertThat(itemCHW.getGridOne().getColumnCode(), is(ALGridViewModel.ALColumnCode.OneColumn));
        assertThat(itemCHW.getGridTwo().getColumnCode(), is(ALGridViewModel.ALColumnCode.TwoColumn));
        assertThat(itemCHW.getGridThree().getColumnCode(), is(ALGridViewModel.ALColumnCode.ThreeColumn));
        assertThat(itemCHW.getGridFour().getColumnCode(), is(ALGridViewModel.ALColumnCode.FourColumn));

        assertThat(itemTotal.getGridOne().getColumnCode(), is(ALGridViewModel.ALColumnCode.OneColumn));
        assertThat(itemTotal.getGridTwo().getColumnCode(), is(ALGridViewModel.ALColumnCode.TwoColumn));
        assertThat(itemTotal.getGridThree().getColumnCode(), is(ALGridViewModel.ALColumnCode.ThreeColumn));
        assertThat(itemTotal.getGridFour().getColumnCode(), is(ALGridViewModel.ALColumnCode.FourColumn));
    }

    @Test
    public void shouldALViewArchEmptyDisplayed() {
        alRequisitionPresenter.rnRForm = null;
        alRequisitionPresenter.updateFormUI();
        assertNull(alRequisitionPresenter.alReportViewModel);
    }

    @Test
    public void shouldSetViewModles() throws LMISException {
        RnRForm rnRForm = createRnrForm(RnRForm.Emergency.No);
        alRequisitionPresenter.rnRForm = rnRForm;
        alRequisitionPresenter.updateFormUI();
        alRequisitionPresenter.setViewModels();
        assertFalse(alRequisitionPresenter.isComplete());
        assertThat(alRequisitionPresenter.getCompleteErrorMessage(), is(R.string.hint_al_complete_failed));
        assertThat(alRequisitionPresenter.rnRForm.getRegimenItemListWrapper().size(), is(8));
    }

    @Test
    public void shouldInitALViewModel() throws LMISException {
        RnRForm rnRForm = mock(RnRForm.class);
        when(mockALRepository.queryRnRForm(1L)).thenReturn(rnRForm);
        when(rnRForm.getRnrItems(Product.IsKit.No)).thenReturn(new ArrayList<RnrFormItem>());

        RnrFormItem rnrKitItem1 = new RnrFormItemBuilder()
                .setProduct(new ProductBuilder().setCode("26A01").build())
                .setReceived(100)
                .setIssued((long) 50)
                .build();
        RnrFormItem rnrKitItem2 = new RnrFormItemBuilder()
                .setProduct(new ProductBuilder().setCode("26A02").build())
                .setReceived(300)
                .setIssued((long) 110)
                .build();
        List<RnrFormItem> rnrFormItems = Lists.newArrayList(rnrKitItem1, rnrKitItem2);
        when(rnRForm.getRnrItems(Product.IsKit.Yes)).thenReturn(rnrFormItems);
        TestSubscriber<RnRForm> testSubscriber = new TestSubscriber<>();
        alRequisitionPresenter.getRnrFormObservable(1L).subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertNoErrors();
    }

    private class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(ALRepository.class).toInstance(mockALRepository);
            bind(RnrFormRepository.class).toInstance(mockRnrFormRepository);
        }
    }
}
