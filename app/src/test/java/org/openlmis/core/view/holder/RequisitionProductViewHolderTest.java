package org.openlmis.core.view.holder;

import android.view.LayoutInflater;
import android.view.View;

import com.google.inject.Binder;
import com.google.inject.Module;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.builder.RnrFormItemBuilder;
import org.openlmis.core.presenter.VIARequisitionPresenter;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.viewmodel.RequisitionFormItemViewModel;
import org.robolectric.RuntimeEnvironment;

import java.sql.Date;

import roboguice.RoboGuice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class RequisitionProductViewHolderTest {

    private RequisitionProductViewHolder viewHolder;
    private VIARequisitionPresenter presenter;
    private Program program;
    private RnRForm form;
    @Before
    public void setUp() {
        View itemView = LayoutInflater.from(RuntimeEnvironment.application).inflate(R.layout.item_requisition_body_left, null, false);
        viewHolder = new RequisitionProductViewHolder(itemView);
        program = new Program();
        program.setProgramCode("ESS_MEDS");
        program.setProgramName("ESS_MEDS");
        form = RnRForm.init(program, DateUtil.today());
        form.setPeriodBegin(Date.valueOf("2015-04-21"));
        form.setPeriodEnd(Date.valueOf("2015-05-20"));

        presenter = mock(VIARequisitionPresenter.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new Module() {
            @Override
            public void configure(Binder binder) {
                binder.bind(VIARequisitionPresenter.class).toInstance(presenter);
            }
        });
        when(presenter.getRnRForm()).thenReturn(form);
        when(presenter.getRnrFormStatus()).thenReturn(RnRForm.STATUS.DRAFT);
    }

    @Test
    public void shouldSetProductNameAndCode() {
        RnrFormItem formItem = new RnrFormItemBuilder().setProduct(
                new ProductBuilder().setPrimaryName("productName").setCode("08S42").build())
                .build();
        RequisitionFormItemViewModel viewModel = new RequisitionFormItemViewModel(formItem);
        viewHolder.populate(viewModel,presenter);

        assertThat(viewHolder.productName.getText().toString()).isEqualTo("productName");
        assertThat(viewHolder.productCode.getText().toString()).isEqualTo("08S42");
    }

    @Test
    public void shouldSetDelIconForNewAddedProduct() throws Exception {
        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_add_drugs_to_via_form,true);

        RnrFormItem formItem = new RnrFormItemBuilder().setProduct(
                new ProductBuilder().setPrimaryName("productName").setCode("08S42").build()).setRnrForm(null)
                .build();

        RequisitionFormItemViewModel viewModel = new RequisitionFormItemViewModel(formItem);
        viewHolder.populate(viewModel, presenter);

        assertThat(viewHolder.ivDelete.getVisibility()).isEqualTo(View.VISIBLE);
    }

    @Test
    public void shouldNotShowDelIconForOldProduct() throws Exception {
        RnrFormItem formItem = new RnrFormItemBuilder().setProduct(
                new ProductBuilder().setPrimaryName("productName").setCode("08S42").build()).setRnrForm(new RnRForm())
                .build();

        RequisitionFormItemViewModel viewModel = new RequisitionFormItemViewModel(formItem);
        viewHolder.populate(viewModel, presenter);

        assertThat(viewHolder.ivDelete.getVisibility()).isEqualTo(View.INVISIBLE);
    }
}