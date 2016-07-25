package org.openlmis.core.view.holder;

import android.view.LayoutInflater;
import android.view.View;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.builder.RnrFormItemBuilder;
import org.openlmis.core.presenter.VIARequisitionPresenter;
import org.openlmis.core.view.viewmodel.RequisitionFormItemViewModel;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(LMISTestRunner.class)
public class RequisitionProductViewHolderTest {

    private RequisitionProductViewHolder viewHolder;
    private VIARequisitionPresenter presenter;

    @Before
    public void setUp() {
        View itemView = LayoutInflater.from(RuntimeEnvironment.application).inflate(R.layout.item_requisition_body_left, null, false);
        viewHolder = new RequisitionProductViewHolder(itemView);
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

        assertThat(viewHolder.ivDelete.getVisibility()).isEqualTo(View.GONE);
    }
}