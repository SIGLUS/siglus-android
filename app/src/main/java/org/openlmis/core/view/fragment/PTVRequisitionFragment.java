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
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.openlmis.core.R;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.presenter.BaseReportPresenter;
import org.openlmis.core.presenter.PTVRequisitionPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.SimpleTextWatcher;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.widget.PTVTestLeftHeader;
import org.openlmis.core.view.widget.PTVTestRnrForm;
import org.openlmis.core.view.widget.SingleClickButtonListener;
import roboguice.RoboGuice;
import roboguice.inject.InjectView;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;

public class PTVRequisitionFragment extends BaseReportFragment implements
    PTVRequisitionPresenter.PTVRequisitionView {

  private long formId;
  protected View containerView;
  private Date periodEndDate;
  PTVRequisitionPresenter presenter;

  @InjectView(R.id.scrollView)
  HorizontalScrollView scrollView;

  @InjectView(R.id.ptv_table)
  PTVTestRnrForm ptvTable;

  @InjectView(R.id.ptv_monthTitle)
  TextView monthTitle;

  @InjectView(R.id.ptv_table_header)
  LinearLayout llTableHeader;

  @InjectView(R.id.ptv_title)
  LinearLayout llTitle;

  @InjectView(R.id.ll_medicines)
  LinearLayout medicines;

  @InjectView(R.id.et_total_parent)
  EditText totalParent;

  @InjectView(R.id.et_total_child)
  EditText totalChild;

  @InjectView(R.id.ptv_left_header)
  PTVTestLeftHeader ptvTestLeftHeader;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    formId = getActivity().getIntent().getLongExtra(Constants.PARAM_FORM_ID, 0);
    periodEndDate = ((Date) getActivity().getIntent()
        .getSerializableExtra(Constants.PARAM_SELECTED_INVENTORY_DATE));
  }


  @Override
  protected BaseReportPresenter injectPresenter() {
    presenter = RoboGuice.getInjector(getActivity()).getInstance(PTVRequisitionPresenter.class);
    return presenter;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    containerView = inflater.inflate(R.layout.fragment_ptv_requisition, container, false);
    return containerView;
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
      presenter.loadData(formId, periodEndDate);
    }
  }

  protected void initUI() {
    scrollView.setVisibility(View.INVISIBLE);
    if (isHistoryForm()) {
      scrollView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
      actionPanelView.setVisibility(View.GONE);
    } else {
      scrollView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
      actionPanelView.setVisibility(View.VISIBLE);
    }

    bindListeners();
    addRegimenListeners();
  }

  @Override
  public void onDestroyView() {
    ptvTable.removeListenerOnDestroyView();
    super.onDestroyView();
  }

  private void addRegimenListeners() {
    PTVRequisitionFragment.EditTextWatcher parentTextWatcher = new PTVRequisitionFragment.EditTextWatcher(
        totalParent);
    totalParent.addTextChangedListener(parentTextWatcher);
    PTVRequisitionFragment.EditTextWatcher childTextWatcher = new PTVRequisitionFragment.EditTextWatcher(
        totalChild);
    totalChild.addTextChangedListener(childTextWatcher);
  }

  private void bindListeners() {
    actionPanelView.setListener(getOnCompleteListener(), getOnSaveListener());
  }

  @NonNull
  private SingleClickButtonListener getOnSaveListener() {
    return new SingleClickButtonListener() {
      @Override
      public void onSingleClick(View v) {
        loading();
        Subscription subscription = presenter.getSaveFormObservable()
            .subscribe(getOnSavedSubscriber());
        subscriptions.add(subscription);
      }
    };
  }

  @NonNull
  public Subscriber<Void> getOnSavedSubscriber() {
    return new Subscriber<Void>() {
      @Override
      public void onCompleted() {
        loaded();
        finish();
      }

      @Override
      public void onError(Throwable e) {
        loaded();
        ToastUtil.show(getString(R.string.hint_save_mmia_failed));
      }

      @Override
      public void onNext(Void aVoid) {
        // do nothing
      }
    };
  }

  @NonNull
  private SingleClickButtonListener getOnCompleteListener() {
    return new SingleClickButtonListener() {
      @Override
      public void onSingleClick(View v) {
        if (ptvTable.isCompleted() && completeRegimen()) {
          scrollView.requestFocus();
          if (!presenter.validateFormPeriod()) {
            ToastUtil.show(R.string.msg_requisition_not_unique);
          } else {
            showSignDialog();
          }
        } else {
          ToastUtil.showForLongTime(R.string.msg_uncompleted_ptv_hint);
        }
      }
    };
  }

  private boolean isHistoryForm() {
    return formId != 0;
  }


  @Override
  public void setProcessButtonName(String buttonName) {
    actionPanelView.setPositiveButtonText(buttonName);
  }

  @Override
  public void completeSuccess() {
    ToastUtil.showForLongTime(R.string.msg_ptv_submit_tip);
    finish();

  }

  @Override
  protected String getSignatureDialogTitle() {
    return presenter.isDraftOrDraftMissed() ? getResources()
        .getString(R.string.msg_ptv_submit_signature)
        : getResources().getString(R.string.msg_approve_signature_ptv);
  }

  @Override
  protected Action1<? super Void> getOnSignedAction() {
    return (Action1<Void>) aVoid -> {
      if (presenter.getRnRForm().isSubmitted()) {
        presenter.submitRequisition();
        showMessageNotifyDialog();
      } else {
        presenter.authoriseRequisition();
      }
    };
  }

  @Override
  protected String getNotifyDialogMsg() {
    return getString(R.string.msg_requisition_signature_message_notify_ptv);

  }

  @Override
  public void refreshRequisitionForm(RnRForm rnRForm) {
    getActivity().setTitle(getString(R.string.label_ptv_title,
        DateUtil.formatDateWithoutYear(rnRForm.getPeriodBegin()),
        DateUtil.formatDateWithoutYear(rnRForm.getPeriodEnd())));
    monthTitle.setText(DateUtil.formatDateWithLongMonthAndYear(rnRForm.getPeriodEnd()));
    scrollView.setVisibility(View.VISIBLE);

    if (!presenter.ptvReportViewModel.isEmpty()) {
      ptvTable.initView(presenter.ptvReportViewModel);
      ptvTestLeftHeader.initView(presenter.ptvReportViewModel);
      refreshRegimenValue();
      refreshUI(rnRForm);
    } else {
      scrollView.setVisibility(View.GONE);
    }
  }

  private void refreshUI(RnRForm rnRForm) {
    int medicineWidth = (int) (getResources().getDimension(R.dimen.ptv_view_item_width)
        + getResources().getDimension(R.dimen.border_width))
        * rnRForm.getRnrFormItemListWrapper().size();
    int viewWidth = (int) (getResources().getDimension(R.dimen.ptv_view_right_header_width)
        + medicineWidth);
    medicines.getLayoutParams().width = medicineWidth;
    llTitle.getLayoutParams().width = viewWidth;
    llTableHeader.getLayoutParams().width = viewWidth;
  }

  private void refreshRegimenValue() {
    RegimenItem regimenAdult = getRegimenItem(Constants.PTV_REGIME_ADULT);
    totalParent.setText(getValue(regimenAdult));
    RegimenItem regimenChild = getRegimenItem(Constants.PTV_REGIME_CHILD);
    totalChild.setText(getValue(regimenChild));
  }

  private boolean completeRegimen() {
    List<EditText> editTexts = Arrays.asList(totalParent, totalChild);

    for (EditText editText : editTexts) {
      if (TextUtils.isEmpty(editText.getText().toString())) {
        editText.setError(getString(R.string.hint_error_input));
        return false;
      }
    }
    return true;
  }

  private String getValue(RegimenItem item) {
    String returnValue = "";
    if (item != null) {
      Long amount = item.getAmount();
      returnValue = amount == null ? "" : String.valueOf(amount);
    }
    return returnValue;
  }

  @Override
  protected void finish() {
    getActivity().setResult(Activity.RESULT_OK);
    getActivity().finish();
  }

  private RegimenItem getRegimenItem(String code) {
    for (RegimenItem regimenItem : presenter.ptvReportViewModel.getForm()
        .getRegimenItemListWrapper()) {
      if (regimenItem.getRegimen().getCode().equals(code)) {
        return regimenItem;
      }
    }
    return null;
  }

  class EditTextWatcher extends SimpleTextWatcher {

    private final EditText editText;

    public EditTextWatcher(EditText editText) {
      this.editText = editText;
    }

    @Override
    public void afterTextChanged(Editable etText) {
      switch (editText.getId()) {
        case R.id.et_total_parent:
          RegimenItem regimenAdult = getRegimenItem(Constants.PTV_REGIME_ADULT);
          if (regimenAdult != null) {
            regimenAdult.setAmount(getEditValue(etText));
          }
          break;
        case R.id.et_total_child:
          RegimenItem regimenChild = getRegimenItem(Constants.PTV_REGIME_CHILD);
          if (regimenChild != null) {
            regimenChild.setAmount(getEditValue(etText));
          }
          break;
        default:
          // do nothing
      }
    }

    private Long getEditValue(Editable etText) {
      Long editText;
      try {
        editText = Long.valueOf(etText.toString());
      } catch (NumberFormatException e) {
        editText = null;
      }
      return editText;
    }
  }

}
