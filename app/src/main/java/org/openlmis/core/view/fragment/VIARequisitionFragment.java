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

import static org.openlmis.core.utils.Constants.REQUEST_ADD_DRUGS_TO_VIA;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.presenter.BaseReportPresenter;
import org.openlmis.core.presenter.VIARequisitionPresenter;
import org.openlmis.core.presenter.VIARequisitionView;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.activity.AddDrugsToVIAActivity;
import org.openlmis.core.view.viewmodel.RequisitionFormItemViewModel;
import org.openlmis.core.view.widget.SingleClickButtonListener;
import org.openlmis.core.view.widget.ViaKitView;
import org.openlmis.core.view.widget.ViaReportConsultationNumberView;
import org.openlmis.core.view.widget.ViaRequisitionBodyView;
import org.roboguice.shaded.goole.common.collect.FluentIterable;
import roboguice.RoboGuice;
import roboguice.inject.InjectView;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;

public class VIARequisitionFragment extends BaseReportFragment implements VIARequisitionView {

  @InjectView(R.id.view_consultation)
  ViaReportConsultationNumberView consultationView;

  @InjectView(R.id.vg_kit)
  ViaKitView kitView;

  @InjectView(R.id.view_via_body)
  ViaRequisitionBodyView bodyView;

  @InjectView(R.id.vg_container)
  ViewGroup vgContainer;

  @Inject
  VIARequisitionPresenter presenter;

  private long formId;

  private Date periodEndDate;
  private boolean isMissedPeriod;
  private ArrayList<StockCard> emergencyStockCards;

  private Menu menu;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);

    formId = getActivity().getIntent().getLongExtra(Constants.PARAM_FORM_ID, 0);
    periodEndDate = ((Date) getActivity().getIntent()
        .getSerializableExtra(Constants.PARAM_SELECTED_INVENTORY_DATE));
    isMissedPeriod = getActivity().getIntent()
        .getBooleanExtra(Constants.PARAM_IS_MISSED_PERIOD, false);
    emergencyStockCards = (ArrayList<StockCard>) getActivity().getIntent()
        .getSerializableExtra(Constants.PARAM_SELECTED_EMERGENCY);
  }

  @Override
  protected BaseReportPresenter injectPresenter() {
    presenter = RoboGuice.getInjector(getActivity()).getInstance(VIARequisitionPresenter.class);
    return presenter;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_via_requisition, container, false);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    if (SharedPreferenceMgr.getInstance().shouldSyncLastYearStockData()) {
      ToastUtil.showInCenter(R.string.msg_stock_movement_is_not_ready);
      finish();
      return;
    }

    initUI();

    if (isSavedInstanceState && presenter.getRnRForm() != null) {
      presenter.updateFormUI();
    } else {
      loadData();
    }
    bodyView.autoScrollLeftToRight();
  }

  public void hideOrShowAddProductMenuInVIAPage() {
    menu.findItem(R.id.action_add_new_drugs_to_via).setVisible(presenter.isFormProductEditable());
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.menu_via_requisition, menu);
    this.menu = menu;
    hideOrShowAddProductMenuInVIAPage();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.action_add_new_drugs_to_via) {
      ArrayList<String> productCodes = new ArrayList<>(FluentIterable
          .from(presenter.getRequisitionFormItemViewModels())
          .transform(RequisitionFormItemViewModel::getFmn)
          .toList());

      startActivityForResult(
          AddDrugsToVIAActivity.getIntentToMe(getActivity(), presenter.getRnRForm().getPeriodBegin(), productCodes),
          REQUEST_ADD_DRUGS_TO_VIA);
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void loadData() {
    if (isFromSelectEmergencyPage()) {
      presenter.loadEmergencyData(emergencyStockCards,
          new Date(LMISApp.getInstance().getCurrentTimeMillis()));
    } else {
      presenter.loadData(formId, periodEndDate);
    }
  }

  private boolean isFromSelectEmergencyPage() {
    return emergencyStockCards != null;
  }

  @Override
  public void refreshRequisitionForm(RnRForm rnRForm) {
    bodyView.refresh(rnRForm);

    if (rnRForm.isEmergency()) {
      refreshEmergencyRnr(rnRForm);
    } else {
      refreshNormalRnr(rnRForm);
    }
    setEditable();
  }

  private void refreshNormalRnr(RnRForm rnRForm) {
    consultationView.refreshNormalRnrConsultationView(presenter);
    actionPanelView.setNegativeButtonVisibility(View.VISIBLE);
    setTitleWithPeriod(rnRForm);
    setKitValues();
  }

  private void refreshEmergencyRnr(RnRForm rnRForm) {
    if (!rnRForm.isAuthorized()) {
      View.OnClickListener onClickListener = v -> ToastUtil
          .showForLongTime(R.string.msg_emergency_requisition_cant_edit);
      consultationView.setEditClickListener(onClickListener);
      kitView.setEditClickListener(onClickListener);
    }

    kitView.setEmergencyKitValues();
    consultationView.setEmergencyRnrHeader();

    getActivity().setTitle(getString(R.string.label_emergency_requisition_title,
        DateUtil.formatDateWithoutYear(new Date(LMISApp.getInstance().getCurrentTimeMillis()))));
    actionPanelView.setNegativeButtonVisibility(View.GONE);
  }

  public void setTitleWithPeriod(RnRForm rnRForm) {
    if (rnRForm != null) {
      getActivity().setTitle(getString(R.string.label_requisition_title,
          DateUtil.formatDateWithoutYear(rnRForm.getPeriodBegin()),
          DateUtil.formatDateWithoutYear(rnRForm.getPeriodEnd())));
    } else {
      getActivity().setTitle(getString(R.string.title_requisition));
    }
  }

  @Override
  public void highLightApprovedAmount() {
    bodyView.highLightApprovedAmount();
  }

  @Override
  public void highLightRequestAmount() {
    bodyView.highLightRequestAmount();
  }

  public void setEditable() {
    if (presenter.getRnRForm().isAuthorized()) {
      vgContainer.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
      actionPanelView.setVisibility(View.GONE);
    } else {
      vgContainer.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
      actionPanelView.setVisibility(View.VISIBLE);
    }
    if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_training)) {
      bodyView.setEditable(false);
    } else {
      bodyView.setEditable(isMissedPeriod || presenter.getRnRForm().isMissed());
    }
  }

  private void setKitValues() {
    kitView.setValue(presenter.getViaKitsViewModel());
  }

  @Override
  public void showListInputError(int index) {
    bodyView.showListInputError(index);
  }

  private void initUI() {
    bodyView.initUI(presenter);
    consultationView.initUI();
    bindListeners();
  }

  private void bindListeners() {
    actionPanelView.setListener(getOnCompleteClickListener(), getOnSaveClickListener());
    bodyView.setHideImmOnTouchListener();
  }

  @NonNull
  private SingleClickButtonListener getOnCompleteClickListener() {
    return new SingleClickButtonListener() {
      @Override
      public void onSingleClick(View v) {
        if (presenter.processRequisition(consultationView.getValue())) {
          showSignDialog();
        }
      }
    };
  }

  @NonNull
  private SingleClickButtonListener getOnSaveClickListener() {
    return new SingleClickButtonListener() {
      @Override
      public void onSingleClick(View v) {
        loading();
        Subscription subscription = presenter.getSaveFormObservable(consultationView.getValue())
            .subscribe(getOnSavedSubscriber());
        subscriptions.add(subscription);
      }
    };
  }

  @NonNull
  public Subscriber<RnRForm> getOnSavedSubscriber() {
    return new Subscriber<RnRForm>() {
      @Override
      public void onCompleted() {
        loaded();
        finish();
      }

      @Override
      public void onError(Throwable e) {
        loaded();
        ToastUtil.show(getString(R.string.hint_save_requisition_failed));
      }

      @Override
      public void onNext(RnRForm rnRForm) {
        // do nothing
      }
    };
  }

  @Override
  public void setProcessButtonName(String buttonName) {
    actionPanelView.setPositiveButtonText(buttonName);
  }

  @Override
  public boolean validateConsultationNumber() {
    return consultationView.validate();
  }

  @NonNull
  @Override
  public String getSignatureDialogTitle() {
    return presenter.isDraftOrDraftMissed() ? getResources()
        .getString(R.string.msg_via_submit_signature)
        : getResources().getString(R.string.msg_approve_signature_via);
  }

  @Override
  protected Action1<? super Void> getOnSignedAction() {
    return (Action1<Void>) aVoid -> {
      if (presenter.getRnRForm().isSubmitted()) {
        presenter.submitRequisition();
        showMessageNotifyDialog();
      } else {
        presenter.createStockCardsOrUnarchiveAndAddToFormForAdditionalRnrItems();
        presenter.authoriseRequisition();
      }
    };
  }

  @Override
  protected String getNotifyDialogMsg() {
    return getString(R.string.msg_requisition_signature_message_notify_via);
  }

  @Override
  public void completeSuccess() {
    ToastUtil.showForLongTime(R.string.msg_requisition_submit_tip);
    finish();
  }

  @Override
  protected void finish() {
    getActivity().setResult(Activity.RESULT_OK);
    getActivity().finish();
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == Constants.REQUEST_ADD_DRUGS_TO_VIA && resultCode == Activity.RESULT_OK) {
      Date periodBegin = (Date) data.getSerializableExtra(Constants.PARAM_PERIOD_BEGIN);
      if (data.getExtras() != null) {

        List<RnrFormItem> drugInVIAs = (ArrayList<RnrFormItem>) data.getExtras()
            .get(Constants.PARAM_ADDED_DRUGS_TO_VIA);
        presenter.populateAdditionalDrugsViewModels(drugInVIAs, periodBegin);
        bodyView.refreshProductNameList();
      } else {
        new LMISException("VIARequisitionFragment onActivityResult").reportToFabric();
      }
    }
  }
}