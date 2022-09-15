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

package org.openlmis.core.view.fragment;

import static junit.framework.Assert.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.fragment.app.DialogFragment;
import com.google.inject.AbstractModule;
import java.sql.Date;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.presenter.MMTBRequisitionPresenter;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.RobolectricUtils;
import org.openlmis.core.view.activity.MMTBRequisitionActivity;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class MMTBRequisitionFragmentTest {

  private Program program;
  private RnRForm form;
  private MMTBRequisitionPresenter mmtbFormPresenter;
  private MMTBRequisitionFragment mmtbRequisitionFragment;

  @Before
  public void setUp() throws Exception {
    mmtbFormPresenter = mock(MMTBRequisitionPresenter.class);
    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
      @Override
      protected void configure() {
        bind(MMTBRequisitionPresenter.class).toInstance(mmtbFormPresenter);
      }
    });
    SharedPreferenceMgr.getInstance().setShouldSyncLastYearStockCardData(false);
    mmtbRequisitionFragment = getFragment();

    program = new Program();
    program.setProgramCode("MMTB");
    program.setProgramName("MMTB");
    form = RnRForm.init(program, DateUtil.today());
    form.setId(1L);
    form.setComments("");
    when(mmtbFormPresenter.getRnrForm(anyInt())).thenReturn(form);
  }

  private MMTBRequisitionFragment getFragment() {
    MMTBRequisitionActivity mmtbRequisitionActivity = Robolectric
        .buildActivity(MMTBRequisitionActivity.class)
        .create()
        .start()
        .resume()
        .get();
    return (MMTBRequisitionFragment) mmtbRequisitionActivity
        .getSupportFragmentManager()
        .findFragmentById(R.id.fragment_requisition);
  }

  @Test
  public void shouldShowRequisitionPeriodOnTitle() {
    form.setPeriodBegin(Date.valueOf("2015-04-21"));
    form.setPeriodEnd(Date.valueOf("2015-05-20"));
    mmtbRequisitionFragment.refreshRequisitionForm(form);
    assertThat(mmtbRequisitionFragment.requireActivity().getTitle()).isEqualTo("MMTB - 21 Apr to 20 May");
  }

  @Test
  public void shouldNotRemoveRnrFormWhenGoBack() {
    mmtbRequisitionFragment.onBackPressed();
    verify(mmtbFormPresenter, never()).deleteDraft();
  }

  @Test
  public void shouldShowConfirmDialogWhenIsDraft() {
    // given
    when(mmtbFormPresenter.isDraft()).thenReturn(true);

    // when
    mmtbRequisitionFragment.onBackPressed();

    // then
    RobolectricUtils.waitLooperIdle();
    DialogFragment fragment = (DialogFragment) mmtbRequisitionFragment
        .getParentFragmentManager()
        .findFragmentByTag("back_confirm_dialog");

    assertThat(fragment).isNotNull();
    assertThat(fragment.getDialog()).isNotNull();
  }

  @Test
  public void shouldFinishWhenIsNotDraft() {
    // given
    when(mmtbFormPresenter.isDraft()).thenReturn(false);

    // when
    mmtbRequisitionFragment.onBackPressed();

    // then
    assertTrue(mmtbRequisitionFragment.requireActivity().isFinishing());
  }
}
