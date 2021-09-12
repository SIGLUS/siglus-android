package org.openlmis.core.view.widget;

import static org.junit.Assert.assertEquals;

import android.view.View;
import com.google.inject.Binder;
import com.google.inject.Module;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.enumeration.OrderStatus;
import org.openlmis.core.model.Pod;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.builder.PodBuilder;
import org.openlmis.core.view.viewmodel.IssueVoucherReportViewModel;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class OrderInfoViewTest {
  private OrderInfoView orderInfoView;

  @Before
  public void setUp() throws Exception {
    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyMode());
    orderInfoView = new OrderInfoView(LMISTestApp.getContext());
  }

  @Test
  public void shouldCorrectUiForShipped() throws Exception {
    // given
    Pod pod = PodBuilder.generatePod();
    pod.setOrderStatus(OrderStatus.SHIPPED);
    pod.setRequisitionActualStartDate(new Date());
    pod.setRequisitionActualEndDate(new Date());
    IssueVoucherReportViewModel viewModel = new IssueVoucherReportViewModel(pod);
    Program program = new Program();
    program.setProgramName("programName");
    viewModel.setProgram(program);

    // when
    orderInfoView.refresh(pod, viewModel);

    //then
    assertEquals(View.GONE, orderInfoView.getLinearLayout().getVisibility());

  }

  @Test
  public void shouldCorrectUiForReceived() throws Exception {
    // given
    Pod pod = PodBuilder.generatePod();
    pod.setOrderStatus(OrderStatus.RECEIVED);
    IssueVoucherReportViewModel viewModel = new IssueVoucherReportViewModel(pod);
    Program program = new Program();
    program.setProgramName("programName");
    viewModel.setProgram(program);

    // when
    orderInfoView.refresh(pod, viewModel);

    //then
    assertEquals(View.VISIBLE, orderInfoView.getLinearLayout().getVisibility());

  }

  private class MyMode implements Module {

    @Override
    public void configure(Binder binder) {
    }
  }
}
