package org.openlmis.core.view.activity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openlmis.core.utils.Constants.PARAM_ISSUE_VOUCHER;
import static org.openlmis.core.view.activity.AddProductsToBulkEntriesActivity.IS_FROM_BULK_ISSUE;
import static org.robolectric.Shadows.shadowOf;

import android.content.Intent;
import android.view.MenuItem;
import androidx.fragment.app.Fragment;
import com.google.inject.AbstractModule;
import java.util.Collections;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.constant.IntentConstants;
import org.openlmis.core.model.Pod;
import org.openlmis.core.presenter.IssueVoucherDraftPresenter;
import org.openlmis.core.utils.RobolectricUtils;
import org.openlmis.core.view.adapter.IssueVoucherDraftProductAdapter;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class IssueVoucherDraftActivityTest {

  private IssueVoucherDraftActivity issueVoucherDraftActivity;
  private IssueVoucherDraftPresenter mockedPresenter;
  private IssueVoucherDraftProductAdapter mockAdapter;
  private ActivityController<IssueVoucherDraftActivity> activityController;

  @Before
  public void setUp() throws Exception {
    mockedPresenter = mock(IssueVoucherDraftPresenter.class);
    mockAdapter = mock(IssueVoucherDraftProductAdapter.class);
    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
      @Override
      protected void configure() {
        bind(IssueVoucherDraftPresenter.class).toInstance(mockedPresenter);
      }
    });
    activityController = Robolectric.buildActivity(IssueVoucherDraftActivity.class);
    issueVoucherDraftActivity = activityController.create().start().resume().get();
    issueVoucherDraftActivity.issueVoucherDraftProductAdapter = mockAdapter;
  }

  @After
  public void teardown() {
    activityController.pause().stop().destroy();
    RoboGuice.Util.reset();
  }

  @Test
  public void shouldCorrectOpenAddProducts() {
    // given
    MenuItem mockMenuItem = mock(MenuItem.class);
    when(mockMenuItem.getItemId()).thenReturn(R.id.action_add_product);
    when(mockedPresenter.getAddedProductCodeList()).thenReturn(Collections.singletonList(""));
    issueVoucherDraftActivity.setProgramCode("VC");

    // when
    issueVoucherDraftActivity.onOptionsItemSelected(mockMenuItem);
    RobolectricUtils.waitLooperIdle();

    // then
    Intent intent = shadowOf(issueVoucherDraftActivity).getNextStartedActivity();
    assertNotNull(intent);
    assertEquals(AddProductsToBulkEntriesActivity.class.getName(), intent.getComponent().getClassName());
    assertFalse(intent.getBooleanExtra(IS_FROM_BULK_ISSUE, false));
    assertNotNull(intent.getSerializableExtra(IntentConstants.CHOSEN_PROGRAM_CODE));
  }

  @Test
  public void shouldCorrectOpenIssueVoucherReportPage() {
    // given
    when(mockedPresenter.coverToPodFromIssueVoucher(anyString())).thenReturn(new Pod());
    when(mockAdapter.validateAll()).thenReturn(-1);
    RobolectricUtils.resetNextClickTime();

    // when
    issueVoucherDraftActivity.getBtNext().performClick();
    RobolectricUtils.waitLooperIdle();

    // then
    Intent intent = shadowOf(issueVoucherDraftActivity).getNextStartedActivity();
    assertNotNull(intent);
    assertEquals(IssueVoucherReportActivity.class.getName(), intent.getComponent().getClassName());
    assertNotNull(intent.getSerializableExtra(PARAM_ISSUE_VOUCHER));
  }

  @Test
  public void shouldShowConfirmWhenDeleteClicked() {
    // when
    issueVoucherDraftActivity.onRemove(1);
    RobolectricUtils.waitLooperIdle();

    // then
    Fragment dialog = issueVoucherDraftActivity.getSupportFragmentManager()
        .findFragmentByTag("issue_voucher_delete_confirm_dialog");
    Assert.assertNotNull(dialog);
  }

  @Test
  public void shouldShowConfirmDialogWhenBackPressed() {
    // when
    issueVoucherDraftActivity.onBackPressed();
    RobolectricUtils.waitLooperIdle();

    // then
    Fragment confirmDialog = issueVoucherDraftActivity.getSupportFragmentManager()
        .findFragmentByTag("issue_voucher_back_confirm_dialog");
    Assert.assertNotNull(confirmDialog);
  }

  @Test
  public void shouldClickListenerCorrectSet() {
    // given
    boolean nextButtonListener = issueVoucherDraftActivity.getBtNext().hasOnClickListeners();

    // then
    Assert.assertTrue(nextButtonListener);
  }

}