/*
 * This program is part of the OpenLMIS logistics management information
 * system platform software.
 *
 * Copyright © 2015 ThoughtWorks, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should
 * have received a copy of the GNU Affero General Public License along with
 * this program. If not, see http://www.gnu.org/licenses. For additional
 * information contact info@OpenLMIS.org
 */

package org.openlmis.core.view.activity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.core.view.activity.AddProductsToBulkEntriesActivity.IS_FROM_BULK_ISSUE;
import static org.robolectric.Shadows.shadowOf;

import android.content.Intent;
import android.view.MenuItem;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.google.inject.AbstractModule;
import java.util.Collections;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.presenter.BulkIssuePresenter;
import org.openlmis.core.utils.RobolectricUtils;
import org.openlmis.core.view.adapter.BulkIssueAdapter;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowToast;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class BulkIssueActivityTest {

  private BulkIssueActivity bulkIssueActivity;
  private BulkIssuePresenter mockedPresenter;
  private BulkIssueAdapter mockAdapter;
  private ActivityController<BulkIssueActivity> activityController;

  @Before
  public void setUp() throws Exception {
    mockedPresenter = mock(BulkIssuePresenter.class);
    mockAdapter = mock(BulkIssueAdapter.class);
    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
      @Override
      protected void configure() {
        bind(BulkIssuePresenter.class).toInstance(mockedPresenter);
      }
    });
    activityController = Robolectric.buildActivity(BulkIssueActivity.class);
    bulkIssueActivity = activityController.create().start().resume().get();
    bulkIssueActivity.bulkIssueAdapter = mockAdapter;
  }

  @After
  public void teardown() {
    activityController.pause().stop().destroy();
    RoboGuice.Util.reset();
  }

  @Test
  public void shouldCorrectSaveDraft() {
    // given
    RobolectricUtils.resetNextClickTime();

    // when
    bulkIssueActivity.findViewById(R.id.btn_save).performClick();
    RobolectricUtils.waitLooperIdle();

    // then
    verify(mockedPresenter, times(1)).saveDraft();
  }

  @Test
  public void shouldCorrectScrollToPositionWhenVerifyFailed() {
    // when
    RecyclerView mockRvBulkIssue = mock(RecyclerView.class);
    when(mockAdapter.validateAll()).thenReturn(1);
    bulkIssueActivity.setRvBulkIssue(mockRvBulkIssue);
    RobolectricUtils.resetNextClickTime();

    // then
    bulkIssueActivity.findViewById(R.id.btn_complete).performClick();
    RobolectricUtils.waitLooperIdle();

    // then
    verify(mockRvBulkIssue, times(1)).smoothScrollToPosition(1);
  }

  @Test
  public void shouldShowSignatureDialogWhenCompleteClicked() {
    // when
    when(mockAdapter.validateAll()).thenReturn(-1);
    RobolectricUtils.resetNextClickTime();

    // then
    bulkIssueActivity.findViewById(R.id.btn_complete).performClick();
    RobolectricUtils.waitLooperIdle();

    // then
    Fragment signatureDialog = bulkIssueActivity.getSupportFragmentManager().findFragmentByTag("bulk_issue_signature");
    Assert.assertNotNull(signatureDialog);
  }

  @Test
  public void shouldAdapterUpdateAfterRefreshUI() {
    // when
    bulkIssueActivity.onRefreshViewModels();

    // then
    Mockito.verify(mockAdapter, times(1)).notifyDataSetChanged();
  }

  @Test
  public void shouldFinishOnLoadViewModelsFailed() {
    // when
    bulkIssueActivity.onLoadViewModelsFailed(new NullPointerException());

    // then
    Assert.assertTrue(shadowOf(bulkIssueActivity).isFinishing());
  }

  @Test
  public void shouldCorrectToastOnSaveDraftFinished() {
    // when
    bulkIssueActivity.onSaveDraftFinished(true);

    // then
    Assert.assertEquals("Successfully Saved", ShadowToast.getTextOfLatestToast());

    // when
    bulkIssueActivity.onSaveDraftFinished(false);

    // then
    Assert.assertEquals("Unsuccessfully Saved", ShadowToast.getTextOfLatestToast());
  }

  @Test
  public void shouldShowConfirmDialogWhenBackPressed() {
    // when
    when(mockedPresenter.needConfirm()).thenReturn(true);

    // then
    bulkIssueActivity.onBackPressed();
    RobolectricUtils.waitLooperIdle();

    // then
    Fragment confirmDialog = bulkIssueActivity.getSupportFragmentManager()
        .findFragmentByTag("bulk_issue_back_confirm_dialog");
    Assert.assertNotNull(confirmDialog);
  }

  @Test
  public void shouldCorrectOpenAddProducts() {
    // given
    MenuItem mockMenuItem = mock(MenuItem.class);
    when(mockMenuItem.getItemId()).thenReturn(R.id.action_add_product);
    when(mockedPresenter.getAddedProductCodeList()).thenReturn(Collections.singletonList(""));

    // when
    bulkIssueActivity.onOptionsItemSelected(mockMenuItem);

    // then
    Intent intent = shadowOf(bulkIssueActivity).getNextStartedActivity();

    assertThat(intent).isNotNull();
    assertThat(intent.getComponent().getClassName()).isEqualTo(AddProductsToBulkEntriesActivity.class.getName());
    assertThat(intent.getBooleanExtra(IS_FROM_BULK_ISSUE, false)).isTrue();
  }

  @Test
  public void shouldFinishAfterSaveMovementSuccess() {
    // when
    bulkIssueActivity.onSaveMovementSuccess();

    // then
    assertThat(bulkIssueActivity.isFinishing()).isTrue();
    assertThat(ShadowToast.getTextOfLatestToast())
        .isEqualTo(LMISTestApp.getContext().getString(R.string.msg_complete_successfully));
  }

  @Test
  public void shouldToastMsgAfterSaveMovementFailed() {
    // given
    LMISException lmisException = new LMISException("test msg");
    // when
    bulkIssueActivity.onSaveMovementFailed(lmisException);

    // then
    assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo("test msg");
  }

  @Test
  public void shouldShowConfirmWhenDeleteClicked() {
    // when
    bulkIssueActivity.onRemove(1);
    RobolectricUtils.waitLooperIdle();

    // then
    Fragment dialog = bulkIssueActivity.getSupportFragmentManager()
        .findFragmentByTag("bulk_issue_delete_confirm_dialog");
    Assert.assertNotNull(dialog);
  }
}