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

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.google.android.material.textfield.TextInputLayout;
import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.openlmis.core.R;
import org.openlmis.core.constant.IntentConstants;
import org.openlmis.core.event.ChangeOrderNumberEvent;
import org.openlmis.core.googleanalytics.ScreenName;
import org.openlmis.core.presenter.EditOrderNumberPresenter;
import org.openlmis.core.presenter.EditOrderNumberPresenter.EditOrderNumberView;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.fragment.SimpleDialogFragment;
import org.openlmis.core.view.fragment.SimpleSelectDialogFragment;
import org.openlmis.core.view.widget.SingleClickButtonListener;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_edit_order_number)
public class EditOrderNumberActivity extends BaseActivity implements EditOrderNumberView {

  @InjectPresenter(EditOrderNumberPresenter.class)
  private EditOrderNumberPresenter presenter;

  @InjectView(R.id.tv_old_order_number)
  private TextView tvOldOrderNumber;

  @InjectView(R.id.et_new_order_number)
  private EditText etNewOrderNumber;

  @InjectView(R.id.til_new_order_number)
  private TextInputLayout tilNewOrderNumber;

  @InjectView(R.id.tv_confirm)
  private TextView tvConfirm;

  private String newOrderNumber;

  private String podOrderNumber;

  private final SingleClickButtonListener clickListener = new SingleClickButtonListener() {
    @Override
    public void onSingleClick(View v) {
      if (v.getId() == R.id.et_new_order_number) {
        showOrderNumberList();
      } else if (v.getId() == R.id.tv_confirm) {
        if (StringUtils.isEmpty(newOrderNumber)) {
          tilNewOrderNumber.setError(getString(R.string.msg_new_order_number_error));
          return;
        }
        showConfirmDialog();
      }
    }

    private void showConfirmDialog() {
      SimpleDialogFragment dialogFragment = SimpleDialogFragment.newInstance(
          null,
          getString(R.string.msg_edit_order_number_confirm, newOrderNumber),
          getString(R.string.btn_positive),
          getString(R.string.btn_negative),
          null);
      dialogFragment.setCallBackListener(new SimpleDialogFragment.MsgDialogCallBack() {
        @Override
        public void positiveClick(String tag) {
          presenter.changeOrderNumber(podOrderNumber, newOrderNumber);
        }

        @Override
        public void negativeClick(String tag) {
          // do nothing
        }
      });
      dialogFragment.show(getSupportFragmentManager(), "edit_order_number_confirm_dialog");
    }

    private void showOrderNumberList() {
      Bundle bundle = new Bundle();
      bundle.putStringArray(SimpleSelectDialogFragment.SELECTIONS, presenter.getOrderNumbers().toArray(new String[]{}));
      SimpleSelectDialogFragment orderNumberListDialog = new SimpleSelectDialogFragment();
      orderNumberListDialog.setArguments(bundle);
      orderNumberListDialog.setItemClickListener((parent, view1, position, id) -> {
        newOrderNumber = presenter.getOrderNumbers().get(position);
        tilNewOrderNumber.setError(null);
        etNewOrderNumber.setText(newOrderNumber);
        orderNumberListDialog.dismiss();
      });
      orderNumberListDialog.show(getSupportFragmentManager(), "select_new_order_number");
    }
  };

  @Override
  protected ScreenName getScreenName() {
    return ScreenName.EDIT_ORDER_NUMBER_SCREEN;
  }

  @Override
  protected int getThemeRes() {
    return R.style.AppTheme_AMBER;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    podOrderNumber = getIntent().getStringExtra(IntentConstants.PARAM_ORDER_NUMBER);
    tvOldOrderNumber.setText(podOrderNumber);
    etNewOrderNumber.setOnClickListener(clickListener);
    etNewOrderNumber.setLongClickable(false);
    tvConfirm.setOnClickListener(clickListener);
    presenter.loadData(podOrderNumber);
  }

  @Override
  public void loadDataFailed() {
    ToastUtil.show("load edit order list error");
    finish();
  }

  @Override
  public void changeOrderNumberFailed() {
    ToastUtil.show("change edit order error");
    finish();
  }

  @Override
  public void changeOrderNumberSuccess() {
    EventBus.getDefault().post(new ChangeOrderNumberEvent());
    finish();
  }
}