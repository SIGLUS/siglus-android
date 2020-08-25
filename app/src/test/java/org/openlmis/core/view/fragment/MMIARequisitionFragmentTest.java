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

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.inject.AbstractModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.BaseInfoItem;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.RegimenItemThreeLines;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.presenter.MMIARequisitionPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.activity.MMIARequisitionActivity;
import org.openlmis.core.view.widget.MMIAPatientInfoList;
import org.openlmis.core.view.widget.MMIARegimeListWrap;
import org.openlmis.core.view.widget.MMIARegimeThreeLineList;
import org.openlmis.core.view.widget.MMIARnrFormProductList;
import org.openlmis.core.view.widget.RnrFormHorizontalScrollView;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowToast;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import roboguice.RoboGuice;
import rx.Observable;
import rx.Observer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class MMIARequisitionFragmentTest {

    private MMIARequisitionPresenter mmiaFormPresenter;
    private MMIARequisitionFragment mmiaRequisitionFragment;
    private Program program;
    private RnRForm form;
    private MMIARegimeListWrap regimeListWrap;
    private MMIAPatientInfoList mmiaPatientInfoListView;
    private MMIARnrFormProductList rnrFormList;


    protected ViewGroup mockRightViewGroup;
    private ViewGroup mockRnrItemsHeaderFreeze;

    @Before
    public void setUp() throws Exception {
        mmiaFormPresenter = mock(MMIARequisitionPresenter.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
            @Override
            protected void configure() {
                bind(MMIARequisitionPresenter.class).toInstance(mmiaFormPresenter);
            }
        });

        rnrFormList = mock(MMIARnrFormProductList.class);
        SharedPreferenceMgr.getInstance().setShouldSyncLastYearStockCardData(false);
        mmiaRequisitionFragment = getMMIARequisitionFragmentWithoutIntent();

        regimeListWrap = mock(MMIARegimeListWrap.class);
        mmiaPatientInfoListView = mock(MMIAPatientInfoList.class);
        mockRnrItemsHeaderFreeze = mock(ViewGroup.class);
        mmiaRequisitionFragment.regimeWrap = regimeListWrap;
        mmiaRequisitionFragment.mmiaPatientInfoListView = mmiaPatientInfoListView;
        mmiaRequisitionFragment.rnrFormList = rnrFormList;
        mmiaRequisitionFragment.rnrItemsHeaderFreeze = mockRnrItemsHeaderFreeze;

        when(rnrFormList.getRightHeaderView()).thenReturn(mock(ViewGroup.class));
        when(rnrFormList.getLeftHeaderView()).thenReturn(mock(ViewGroup.class));
        when(rnrFormList.getRnrItemsHorizontalScrollView()).thenReturn(mock(RnrFormHorizontalScrollView.class));

        EditText patientTotalView = mock(EditText.class);
        when(mmiaPatientInfoListView.getPatientTotalView()).thenReturn(patientTotalView);

        program = new Program();
        program.setProgramCode("MMIA");
        program.setProgramName("MMIA");
        form = RnRForm.init(program, DateUtil.today());
        form.setId(1L);
        form.setComments("");
        when(mmiaFormPresenter.getRnrForm(anyInt())).thenReturn(form);
    }

    private MMIARequisitionFragment getMMIARequisitionFragmentWithFormId() {
        Intent intent = new Intent();
        intent.putExtra(Constants.PARAM_FORM_ID, 1L);
        MMIARequisitionActivity mmiaRequisitionActivity = Robolectric.buildActivity(MMIARequisitionActivity.class, intent).create().get();
        MMIARequisitionFragment fragment = (MMIARequisitionFragment) mmiaRequisitionActivity.getFragmentManager().findFragmentById(R.id.fragment_requisition);
        fragment.regimeWrap = regimeListWrap;
        fragment.mmiaPatientInfoListView = mmiaPatientInfoListView;
        fragment.rnrFormList = rnrFormList;

        return fragment;
    }

    private MMIARequisitionFragment getMMIARequisitionFragmentWithoutIntent() {
        MMIARequisitionActivity mmiaRequisitionActivity = Robolectric.buildActivity(MMIARequisitionActivity.class).create().get();
        MMIARequisitionFragment fragment = (MMIARequisitionFragment) mmiaRequisitionActivity.getFragmentManager().findFragmentById(R.id.fragment_requisition);
        fragment.mmiaPatientInfoListView = mmiaPatientInfoListView;
        fragment.rnrFormList = rnrFormList;

        return fragment;
    }

    @Test
    public void shouldInitRegimeAndMMIAListView() {
        ArrayList<RegimenItem> regimenItems = new ArrayList<>();
        ArrayList<BaseInfoItem> baseInfoItems = new ArrayList<>();
        form.setBaseInfoItemListWrapper(baseInfoItems);
        form.setRegimenItemListWrapper(regimenItems);

        mmiaRequisitionFragment.refreshRequisitionForm(form);

        verify(rnrFormList).initView(any(ArrayList.class), anyBoolean());
        verify(regimeListWrap).initView(mmiaRequisitionFragment.tvRegimeTotal,
                mmiaRequisitionFragment.tvRegimeTotalPharmacy,
                mmiaRequisitionFragment.tvTotalPharmacyTitle, mmiaFormPresenter);
        verify(mmiaPatientInfoListView).initView(baseInfoItems);
    }

    @Test
    public void shouldShowTheCannotInitFormToastWhenTheAllStockMovementsAreNotSyncDown() {
        reset(mmiaFormPresenter);
        SharedPreferenceMgr.getInstance().setShouldSyncLastYearStockCardData(true);
        mmiaRequisitionFragment = getMMIARequisitionFragmentWithoutIntent();

        String msg = mmiaRequisitionFragment.getString(R.string.msg_stock_movement_is_not_ready);
        assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo(msg);
        verify(mmiaFormPresenter, never()).loadData(anyLong(), any(Date.class));
    }

    @Test
    public void shouldSaveCompleteWhenMethodCalled() {
        mmiaRequisitionFragment.completeSuccess();

        String successMessage = mmiaRequisitionFragment.getString(R.string.msg_mmia_submit_tip);
        assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo(successMessage);

        assertThat(mmiaRequisitionFragment.getActivity().isFinishing()).isTrue();
    }

    @Test
    public void shouldShowValidationAlertWhenMethodCalled() {
        mmiaRequisitionFragment.showValidationAlert();

        SimpleDialogFragment fragment = (SimpleDialogFragment) mmiaRequisitionFragment.getFragmentManager().findFragmentByTag("not_match_dialog");

        assertThat(fragment).isNotNull();

        final Dialog dialog = fragment.getDialog();
        assertThat(dialog).isNotNull();
    }

    @Test
    public void shouldNotRemoveRnrFormWhenGoBack() {
        mmiaRequisitionFragment.onBackPressed();
        verify(mmiaFormPresenter, never()).deleteDraft();
    }

    @Test
    public void shouldShowSaveAndCompleteButtonWhenFormIsEditable() {
        mmiaRequisitionFragment.initUI();

        assertThat(mmiaRequisitionFragment.actionPanelView.getVisibility()).isEqualTo(View.VISIBLE);
    }

    @Test
    public void shouldNotShowSaveAndCompleteButtonWhenFormIsNotEditable() {
        when(mmiaFormPresenter.isHistoryForm()).thenReturn(true);
        mmiaRequisitionFragment = getMMIARequisitionFragmentWithFormId();

        mmiaRequisitionFragment.initUI();

        assertThat(mmiaRequisitionFragment.actionPanelView.getVisibility()).isEqualTo(View.GONE);
    }

    @Test
    public void shouldSetTitleWithPeriod() {
        form.setPeriodBegin(Date.valueOf("2015-04-21"));
        form.setPeriodEnd(Date.valueOf("2015-05-20"));

        mmiaRequisitionFragment.refreshRequisitionForm(form);

        assertThat(mmiaRequisitionFragment.getActivity().getTitle()).isEqualTo("MMIA - 21 Apr to 20 May");
    }

    @Test
    public void shouldDeHighLightWhenTotalMatches() {
        when(mmiaPatientInfoListView.getTotal()).thenReturn(20L);

        mmiaRequisitionFragment.regimeWrap = regimeListWrap;
        mmiaRequisitionFragment.mmiaPatientInfoListView = mmiaPatientInfoListView;

        mmiaRequisitionFragment.refreshRequisitionForm(form);

        verify(regimeListWrap).deHighLightTotal();
        verify(mmiaPatientInfoListView).deHighLightTotal();
        assertThat(mmiaRequisitionFragment.tvMismatch.getVisibility()).isEqualTo(View.INVISIBLE);
    }


    @Test
    public void shouldDisplayRegimeThreeLine() {
        List<RegimenItemThreeLines> linesList = new ArrayList<>();
        linesList.add(new RegimenItemThreeLines(0, getString(R.string.mmia_1stline)));
        linesList.add(new RegimenItemThreeLines(1, getString(R.string.mmia_2ndline)));
        linesList.add(new RegimenItemThreeLines(2, getString(R.string.mmia_3rdline)));
        form.setRegimenThreeLinesWrapper(linesList);
        mmiaRequisitionFragment.regimeWrap = regimeListWrap;
        mmiaRequisitionFragment.mmiaPatientInfoListView = mmiaPatientInfoListView;

        mmiaRequisitionFragment.refreshRequisitionForm(form);
        verify(regimeListWrap).deHighLightTotal();
        verify(mmiaPatientInfoListView).deHighLightTotal();
        assertThat(mmiaRequisitionFragment.tvMismatch.getVisibility()).isEqualTo(View.INVISIBLE);
        assertFalse(mmiaRequisitionFragment.mmiaRegimeThreeLineListView.isCompleted());
        assertTrue(mmiaRequisitionFragment.mmiaRegimeThreeLineListView.hasEmptyField());
        assertThat(mmiaRequisitionFragment.mmiaRegimeThreeLineListView.getDataList().size()).isEqualTo(3);
        assertThat(mmiaRequisitionFragment.mmiaRegimeThreeLineListView.getTotal(MMIARegimeThreeLineList.COUNTTYPE.PATIENTSAMOUNT)).isEqualTo(0);
    }

    private String getString(int id) {
        return mmiaRequisitionFragment.getString(id);
    }

    @Test
    public void shouldDeHighlightWhenTotalNotMatchesAndLessThanFiveWithEmptyField() {
        when(mmiaPatientInfoListView.getTotal()).thenReturn(40L);
        when(mmiaPatientInfoListView.hasEmptyField()).thenReturn(true);

        form.setComments("ab");

        mmiaRequisitionFragment.regimeWrap = regimeListWrap;
        mmiaRequisitionFragment.mmiaPatientInfoListView = mmiaPatientInfoListView;

        mmiaRequisitionFragment.refreshRequisitionForm(form);

        assertThat(mmiaRequisitionFragment.tvMismatch.getVisibility()).isEqualTo(View.INVISIBLE);
    }

    @Test
    public void shouldDeHighlightWhenTotalNotMatchesAndMoreThanFive() {
        when(mmiaPatientInfoListView.getTotal()).thenReturn(40L);
        when(mmiaPatientInfoListView.hasEmptyField()).thenReturn(false);

        form.setComments("abdasdsa");

        mmiaRequisitionFragment.regimeWrap = regimeListWrap;
        mmiaRequisitionFragment.mmiaPatientInfoListView = mmiaPatientInfoListView;

        mmiaRequisitionFragment.refreshRequisitionForm(form);

        assertThat(mmiaRequisitionFragment.tvMismatch.getVisibility()).isEqualTo(View.INVISIBLE);
    }

    @Test
    public void shouldDeHighlightWhenTotalMatchesAndCommentLengthLessThanFiveAndWithoutEmptyField() {
        when(mmiaPatientInfoListView.getTotal()).thenReturn(20L);
        when(mmiaPatientInfoListView.hasEmptyField()).thenReturn(false);

        mmiaRequisitionFragment.etComment.setText("ab");

        mmiaRequisitionFragment.regimeWrap = regimeListWrap;
        mmiaRequisitionFragment.mmiaPatientInfoListView = mmiaPatientInfoListView;

        mmiaRequisitionFragment.refreshRequisitionForm(form);

        assertThat(mmiaRequisitionFragment.tvMismatch.getVisibility()).isEqualTo(View.INVISIBLE);
    }

    @Test
    public void shouldDeHighlightWhenTotalMatchesWithoutEmptyField() {
        when(mmiaPatientInfoListView.getTotal()).thenReturn(20L);
        when(mmiaPatientInfoListView.hasEmptyField()).thenReturn(false);

        mmiaRequisitionFragment.etComment.setText("abcde");

        mmiaRequisitionFragment.regimeWrap = regimeListWrap;
        mmiaRequisitionFragment.mmiaPatientInfoListView = mmiaPatientInfoListView;

        mmiaRequisitionFragment.refreshRequisitionForm(form);

        assertThat(mmiaRequisitionFragment.tvMismatch.getVisibility()).isEqualTo(View.INVISIBLE);
    }

    @Test
    public void shouldShowSubmitSignatureDialog() {
        when(mmiaFormPresenter.isDraftOrDraftMissed()).thenReturn(true);
        mmiaRequisitionFragment.showSignDialog();

        DialogFragment fragment = (DialogFragment) (mmiaRequisitionFragment.getFragmentManager().findFragmentByTag("signature_dialog"));

        assertThat(fragment).isNotNull();

        Dialog dialog = fragment.getDialog();

        assertThat(dialog).isNotNull();

        String alertMessage = mmiaRequisitionFragment.getString(R.string.msg_mmia_submit_signature);
        assertThat(fragment.getArguments().getString("title")).isEqualTo(alertMessage);
    }

    @Test
    public void shouldShowApproveSignatureDialog() {
        when(mmiaFormPresenter.isDraftOrDraftMissed()).thenReturn(false);

        mmiaRequisitionFragment.showSignDialog();

        DialogFragment fragment = (DialogFragment) (mmiaRequisitionFragment.getFragmentManager().findFragmentByTag("signature_dialog"));

        assertThat(fragment).isNotNull();

        Dialog dialog = fragment.getDialog();

        assertThat(dialog).isNotNull();

        String alertMessage = mmiaRequisitionFragment.getString(R.string.msg_approve_signature_mmia);
        assertThat(fragment.getArguments().getString("title")).isEqualTo(alertMessage);
    }

    @Test
    public void shouldMessageNotifyDialog() {
        mmiaRequisitionFragment.showMessageNotifyDialog();

        DialogFragment fragment = (DialogFragment) (mmiaRequisitionFragment.getFragmentManager().findFragmentByTag("showMessageNotifyDialog"));

        assertThat(fragment).isNotNull();

        AlertDialog dialog = (AlertDialog) fragment.getDialog();

        assertThat(dialog).isNotNull();
    }

    @Test
    public void shouldDisplayFreezeHeaderWhenLocationIsAboveOrAtRnrItemsListWhenScrolling() {
        when(rnrFormList.getHeight()).thenReturn(2000);
        int numberOfItemsInRNR = 10;
        mmiaRequisitionFragment.actionBarHeight = 100;

        mockRightViewGroup = mock(ViewGroup.class);
        when(rnrFormList.getRightViewGroup()).thenReturn(mockRightViewGroup);
        final View mockView = mock(View.class);
        when(mockRightViewGroup.getChildAt(numberOfItemsInRNR - 1)).thenReturn(mockView);
        when(mockView.getHeight()).thenReturn(100);
        when(rnrFormList.getRightViewGroup().getChildCount()).thenReturn(numberOfItemsInRNR);

        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            ((int[]) args[0])[0] = 50;
            ((int[]) args[0])[1] = -1700;
            return null;
        }).when(rnrFormList).getLocationOnScreen(any(int[].class));

        when(mmiaRequisitionFragment.rnrItemsHeaderFreeze.getHeight()).thenReturn(100);

        mmiaRequisitionFragment.hideOrDisplayRnrItemsHeader();
        verify(mockRnrItemsHeaderFreeze).setVisibility(View.VISIBLE);
    }

    @Test
    public void shouldHideFreezeHeaderWhenLocationIsBelowRnrItemsListWhenScrolling() {
        when(rnrFormList.getHeight()).thenReturn(2000);
        int numberOfItemsInRNR = 10;
        mmiaRequisitionFragment.actionBarHeight = 100;

        mockRightViewGroup = mock(ViewGroup.class);
        when(rnrFormList.getRightViewGroup()).thenReturn(mockRightViewGroup);
        final View mockView = mock(View.class);
        when(mockRightViewGroup.getChildAt(numberOfItemsInRNR - 1)).thenReturn(mockView);
        when(mockView.getHeight()).thenReturn(100);
        when(rnrFormList.getRightViewGroup().getChildCount()).thenReturn(numberOfItemsInRNR);

        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            ((int[]) args[0])[0] = 50;
            ((int[]) args[0])[1] = -1800;
            return null;
        }).when(rnrFormList).getLocationOnScreen(any(int[].class));

        when(mmiaRequisitionFragment.rnrItemsHeaderFreeze.getHeight()).thenReturn(100);

        mmiaRequisitionFragment.hideOrDisplayRnrItemsHeader();
        verify(mockRnrItemsHeaderFreeze).setVisibility(View.INVISIBLE);
    }

    @Test
    public void shouldAddCustomRegimenItem() {
        MMIARequisitionFragment mmiaRequisitionFragmentSpy = spy(mmiaRequisitionFragment);

        Regimen regimen = new Regimen();
        Observable<Void> value = Observable.create(Observer::onCompleted);
        when(mmiaFormPresenter.addCustomRegimenItem(regimen)).thenReturn(value);
        RnRForm rnRForm = new RnRForm();
        when(mmiaFormPresenter.getRnRForm()).thenReturn(rnRForm);

        Intent data = new Intent();
        data.putExtra(Constants.PARAM_CUSTOM_REGIMEN, regimen);
        mmiaRequisitionFragmentSpy.onActivityResult(MMIARequisitionFragment.REQUEST_FOR_CUSTOM_REGIME, Activity.RESULT_OK, data);

        verify(mmiaRequisitionFragmentSpy.regimeWrap).addCustomRegimenItem(regimen);
    }
}