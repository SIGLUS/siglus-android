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

package org.openlmis.core.view.widget;

import static org.openlmis.core.view.widget.DoubleListScrollListener.scrollInSync;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ListView;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.google.inject.Inject;
import org.openlmis.core.R;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnRForm.Status;
import org.openlmis.core.presenter.VIARequisitionPresenter;
import org.openlmis.core.utils.keyboard.KeyboardUtil;
import org.openlmis.core.view.adapter.RequisitionFormAdapter;
import org.openlmis.core.view.adapter.RequisitionProductAdapter;
import roboguice.RoboGuice;
import roboguice.inject.InjectView;

public class ViaRequisitionBodyView extends FrameLayout {

  @InjectView(R.id.requisition_form_list_view)
  ListView requisitionFormList;

  @InjectView(R.id.product_name_list_view)
  ListView requisitionProductList;

  @InjectView(R.id.requisition_header_right)
  View bodyHeaderView;

  @InjectView(R.id.requisition_header_left)
  View productHeaderView;

  @InjectView(R.id.form_layout)
  HorizontalScrollView formLayout;

  @InjectView(R.id.tv_label_request)
  TextView headerRequestAmount;

  @InjectView(R.id.tv_label_approve)
  TextView headerApproveAmount;

  private VIARequisitionPresenter presenter;

  @Inject
  Context context;

  private RequisitionFormAdapter requisitionFormAdapter;

  private RequisitionProductAdapter requisitionProductAdapter;

  public ViaRequisitionBodyView(Context context) {
    super(context);
    init(context);
  }

  public ViaRequisitionBodyView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  private void init(Context context) {
    LayoutInflater.from(context).inflate(R.layout.via_requisition_body, this);
    RoboGuice.injectMembers(getContext(), this);
    RoboGuice.getInjector(getContext()).injectViewMembers(this);
  }

  public void showListInputError(final int position) {
    requisitionFormList.setSelection(position);
    requisitionProductList.setSelection(position);
    requisitionFormList.post(() -> {
      View childAt = getViewByPosition(position, requisitionFormList);
      EditText requestAmount = childAt.findViewById(R.id.et_request_amount);
      EditText approvedAmount = childAt.findViewById(R.id.et_approved_amount);
      if (requestAmount.isEnabled()) {
        requestAmount.requestFocus();
        requestAmount.setError(getResources().getString(R.string.hint_error_input));
      } else {
        approvedAmount.requestFocus();
        approvedAmount.setError(getResources().getString(R.string.hint_error_input));
      }
    });
  }

  public void initUI(VIARequisitionPresenter presenter) {
    this.presenter = presenter;
    this.requisitionFormAdapter = new RequisitionFormAdapter(context, presenter);
    this.requisitionProductAdapter = new RequisitionProductAdapter(context, presenter);
    requisitionFormList.setAdapter(requisitionFormAdapter);
    requisitionProductList.setAdapter(requisitionProductAdapter);
    requisitionProductList.post(() -> {
      ViewGroup.LayoutParams layoutParams = productHeaderView.getLayoutParams();
      layoutParams.height = bodyHeaderView.getHeight();
      productHeaderView.setLayoutParams(layoutParams);
    });
    scrollInSync(requisitionFormList, requisitionProductList);
  }

  public void autoScrollLeftToRight() {
    if (!presenter.isHistoryForm()) {
      formLayout.post(() -> formLayout.fullScroll(FOCUS_RIGHT));
    }
  }

  public void refresh(RnRForm rnRForm) {
    refreshProductNameList();
    refreshFormList(rnRForm.getStatus());
  }

  public void setEditable(boolean isRnrFormMissed) {
    if (isRnrFormMissed) {
      requisitionFormList.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
    } else {
      requisitionFormList.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
    }
  }

  public void refreshProductNameList() {
    requisitionProductAdapter.notifyDataSetChanged();
  }

  public void refreshFormList(Status status) {
    requisitionFormAdapter.updateStatus(status);
  }

  public void highLightApprovedAmount() {
    headerRequestAmount.setBackgroundResource(android.R.color.transparent);
    headerRequestAmount.setTextColor(ContextCompat.getColor(getContext(), R.color.color_text_primary));
    headerApproveAmount.setBackgroundResource(R.color.color_accent);
    headerApproveAmount.setTextColor(ContextCompat.getColor(getContext(), R.color.color_white));

    refreshFormList(Status.SUBMITTED);
  }

  public void highLightRequestAmount() {
    headerRequestAmount.setBackgroundResource(R.color.color_accent);
    headerRequestAmount.setTextColor(ContextCompat.getColor(getContext(), R.color.color_white));
    headerApproveAmount.setBackgroundResource(android.R.color.transparent);
    headerApproveAmount.setTextColor(ContextCompat.getColor(getContext(), R.color.color_text_primary));

    refreshFormList(Status.DRAFT);
  }

  public void setHideImmOnTouchListener() {
    formLayout.setOnTouchListener((v, event) -> {
      KeyboardUtil.hideKeyboard(v);
      return false;
    });
  }

  private View getViewByPosition(int pos, ListView listView) {
    final int firstListItemPosition = listView.getFirstVisiblePosition();
    final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

    if (pos < firstListItemPosition || pos > lastListItemPosition) {
      return listView.getAdapter().getView(pos, null, listView);
    } else {
      final int childIndex = pos - firstListItemPosition;
      return listView.getChildAt(childIndex);
    }
  }
}