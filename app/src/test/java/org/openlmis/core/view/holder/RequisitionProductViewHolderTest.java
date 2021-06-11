package org.openlmis.core.view.holder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.view.LayoutInflater;
import android.view.View;
import com.google.inject.Binder;
import com.google.inject.Module;
import java.sql.Date;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnRForm.Status;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.builder.RnrFormItemBuilder;
import org.openlmis.core.presenter.VIARequisitionPresenter;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.RobolectricUtils;
import org.openlmis.core.view.activity.VIARequisitionActivity;
import org.openlmis.core.view.fragment.SimpleDialogFragment;
import org.openlmis.core.view.viewmodel.RequisitionFormItemViewModel;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
@SuppressWarnings("PMD")
public class RequisitionProductViewHolderTest {

  private RequisitionProductViewHolder viewHolder;
  private VIARequisitionPresenter presenter;
  private Program program;
  private RnRForm form;
  private RnrFormItem formItem;

  @Before
  public void setUp() {
    View itemView = LayoutInflater.from(RuntimeEnvironment.application)
        .inflate(R.layout.item_requisition_body_left, null, false);
    viewHolder = new RequisitionProductViewHolder(itemView);
    program = new Program();
    program.setProgramCode("ESS_MEDS");
    program.setProgramName("ESS_MEDS");
    form = RnRForm.init(program, DateUtil.today());
    form.setPeriodBegin(Date.valueOf("2015-04-21"));
    form.setPeriodEnd(Date.valueOf("2015-05-20"));
    formItem = new RnrFormItemBuilder().setProduct(
        new ProductBuilder().setPrimaryName("productName").setCode("08S42").build())
        .setInitialAmount(10)
        .setIssued((long) 10)
        .setAdjustment((long) 10)
        .setInventory((long) 10)
        .setCalculatedOrderQuantity(10)
        .build();

    presenter = mock(VIARequisitionPresenter.class);
    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new Module() {
      @Override
      public void configure(Binder binder) {
        binder.bind(VIARequisitionPresenter.class).toInstance(presenter);
      }
    });
    when(presenter.getRnRForm()).thenReturn(form);
    when(presenter.getRnrFormStatus()).thenReturn(Status.DRAFT);
  }

  @Test
  public void shouldSetProductNameAndCode() {
    RequisitionFormItemViewModel viewModel = new RequisitionFormItemViewModel(formItem);
    viewHolder.populate(viewModel, presenter, LMISTestApp.getContext());

    assertThat(viewHolder.productName.getText().toString()).isEqualTo("productName");
    assertThat(viewHolder.productCode.getText().toString()).isEqualTo("08S42");
  }

  @Test
  public void shouldSetDelIconForNewAddedProduct() throws Exception {
    formItem.setManualAdd(true);
    RequisitionFormItemViewModel viewModel = new RequisitionFormItemViewModel(formItem);
    viewHolder.populate(viewModel, presenter, LMISTestApp.getContext());

    assertThat(viewHolder.ivDelete.getVisibility()).isEqualTo(View.INVISIBLE);
  }

  @Test
  public void shouldNotShowDelIconForOldProduct() throws Exception {
    RequisitionFormItemViewModel viewModel = new RequisitionFormItemViewModel(formItem);
    viewHolder.populate(viewModel, presenter, LMISTestApp.getContext());

    assertThat(viewHolder.ivDelete.getVisibility()).isEqualTo(View.INVISIBLE);
  }

  @Test
  public void shouldShowDialogWhenClickDelIcon() throws Exception {
    VIARequisitionActivity viaRequisitionActivity = Robolectric
        .setupActivity(VIARequisitionActivity.class);
    Product product = new Product();

    viewHolder
        .populate(new RequisitionFormItemViewModel(formItem), presenter, viaRequisitionActivity);

    viewHolder.showDelConfirmDialog(formItem);

    RobolectricUtils.waitLooperIdle();

    SimpleDialogFragment del_confirm_dialog = (SimpleDialogFragment) viaRequisitionActivity
        .getFragmentManager().findFragmentByTag("del_confirm_dialog");
    assertNotNull(del_confirm_dialog);
  }

  @Test
  public void shouldEqual() {
    RequisitionFormItemViewModel viewModel1 = new RequisitionFormItemViewModel(formItem);
    RequisitionFormItemViewModel viewModel = new RequisitionFormItemViewModel(formItem);

    assertEquals(viewModel, viewModel1);
  }

}