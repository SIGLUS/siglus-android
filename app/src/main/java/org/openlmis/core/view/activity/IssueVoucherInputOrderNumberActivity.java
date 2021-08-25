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

import static org.openlmis.core.view.activity.AddProductsToBulkEntriesActivity.CHOSEN_PROGRAM_CODE;
import static org.openlmis.core.view.activity.AddProductsToBulkEntriesActivity.IS_FROM_BULK_ISSUE;
import static org.openlmis.core.view.activity.AddProductsToBulkEntriesActivity.SELECTED_PRODUCTS;
import static org.openlmis.core.view.activity.IssueVoucherActivity.MOVEMENT_REASON_CODE;
import static org.openlmis.core.view.activity.IssueVoucherActivity.ORDER_NUMBER;
import static org.openlmis.core.view.activity.IssueVoucherActivity.PROGRAM_CODE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;
import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.R;
import org.openlmis.core.googleanalytics.ScreenName;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.manager.MovementReasonManager.MovementReason;
import org.openlmis.core.manager.MovementReasonManager.MovementType;
import org.openlmis.core.model.Program;
import org.openlmis.core.presenter.IssueVoucherInputOrderNumberPresenter;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.SimpleTextWatcher;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.fragment.SimpleSelectDialogFragment;
import org.openlmis.core.view.widget.SingleClickButtonListener;
import org.roboguice.shaded.goole.common.collect.FluentIterable;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Subscriber;
import rx.Subscription;

@ContentView(R.layout.activity_issue_voucher_input_order_number)
public class IssueVoucherInputOrderNumberActivity extends BaseActivity {

  @InjectView(R.id.v_white_background)
  private View background;

  @InjectView(R.id.til_order_number)
  private TextInputLayout tilOrderNumber;

  @Getter(AccessLevel.PACKAGE)
  @InjectView(R.id.et_order_number)
  private TextInputEditText etOrderNumber;

  @InjectView(R.id.til_program)
  private TextInputLayout tilProgram;

  @InjectView(R.id.til_origin)
  private TextInputLayout tilOrigin;

  @Getter(AccessLevel.PACKAGE)
  @InjectView(R.id.et_origin)
  private TextInputEditText etOrigin;

  @Getter(AccessLevel.PACKAGE)
  @InjectView(R.id.et_issue_voucher_program)
  private TextInputEditText etProgram;

  @Getter(AccessLevel.PACKAGE)
  @InjectView(R.id.bt_next)
  private Button btNext;

  @Setter(AccessLevel.PACKAGE)
  private String orderNumber = null;

  @Setter(AccessLevel.PACKAGE)
  private MovementReason chosenReason = null;

  @Setter(AccessLevel.PACKAGE)
  private Program chosenProgram = null;

  @InjectPresenter(IssueVoucherInputOrderNumberPresenter.class)
  IssueVoucherInputOrderNumberPresenter presenter;

  private List<Program> programItems = new ArrayList<>();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initProgramData();
    background.setOnClickListener(getBackgroundClickListener());
    etOrigin.setOnClickListener(getMovementReasonOnClickListener());
    btNext.setOnClickListener(getNextClickListener());
    etOrderNumber.addTextChangedListener(new SimpleTextWatcher() {
      @Override
      public void afterTextChanged(Editable s) {
        orderNumber = s.toString();
        if (!StringUtils.isBlank(orderNumber)) {
          tilOrderNumber.setError(null);
        } else {
          tilOrderNumber.setError(getResources().getString(R.string.alert_order_number_can_not_be_blank));
        }
        super.afterTextChanged(s);
      }
    });
  }

  @Override
  protected ScreenName getScreenName() {
    return ScreenName.ISSUE_VOUCHER_INPUT_ORDER_NUMBER_SCREEN;
  }

  @Override
  protected int getThemeRes() {
    return R.style.AppTheme_AMBER;
  }

  private void updateMovementReason() {
    etOrigin.setText(chosenReason == null ? "" : chosenReason.getDescription());
  }

  private void updateProgramItem() {
    etProgram.setText(chosenProgram == null ? "" : chosenProgram.getProgramName());
  }

  protected void initProgramData() {
    final Subscription subscription = presenter.loadPrograms().subscribe(getOnProgramsLoadedSubscriber());
    subscriptions.add(subscription);
  }

  @NonNull
  protected Subscriber<List<Program>> getOnProgramsLoadedSubscriber() {
    return new Subscriber<List<org.openlmis.core.model.Program>>() {
      @Override
      public void onCompleted() {
        // do nothing
      }

      @Override
      public void onError(Throwable e) {
        ToastUtil.show(e.getMessage());
        loaded();
        finish();
      }

      @Override
      public void onNext(List<Program> programs) {
        programItems = programs;
        etProgram.setOnClickListener(getProgramOnClickListener());
      }
    };
  }

  @NonNull
  private View.OnClickListener getProgramOnClickListener() {
    return new SingleClickButtonListener() {
      @Override
      public void onSingleClick(View view) {
        hideKeyboard(view);
        String[] programArray = FluentIterable.from(programItems).transform(Program::getProgramName)
            .toArray(String.class);
        Bundle bundle = new Bundle();
        bundle.putStringArray(SimpleSelectDialogFragment.SELECTIONS, programArray);
        SimpleSelectDialogFragment programDialog = new SimpleSelectDialogFragment();
        programDialog.setArguments(bundle);
        programDialog.setMovementTypeOnClickListener((parent, view1, position, id) -> {
          tilProgram.setError(null);
          chosenProgram = programItems.get(position);
          updateProgramItem();
          programDialog.dismiss();
        });
        programDialog.show(getSupportFragmentManager(), "SELECT_PROGRAMS");
      }
    };
  }

  @NonNull
  private View.OnClickListener getMovementReasonOnClickListener() {
    return new SingleClickButtonListener() {
      @Override
      public void onSingleClick(View view) {
        hideKeyboard(view);
        List<MovementReason> movementReasons = MovementReasonManager.getInstance()
            .buildReasonListForMovementType(MovementType.RECEIVE);
        String[] reasonArray = FluentIterable.from(movementReasons).transform(MovementReason::getDescription)
            .toArray(String.class);
        Bundle bundle = new Bundle();
        bundle.putStringArray(SimpleSelectDialogFragment.SELECTIONS, reasonArray);
        SimpleSelectDialogFragment reasonsDialog = new SimpleSelectDialogFragment();
        reasonsDialog.setArguments(bundle);
        reasonsDialog.setMovementTypeOnClickListener((parent, view1, position, id) -> {
          tilOrigin.setError(null);
          chosenReason = movementReasons.get(position);
          updateMovementReason();
          reasonsDialog.dismiss();
        });
        reasonsDialog.show(getSupportFragmentManager(), "SELECT_REASONS");
      }
    };
  }

  private final ActivityResultLauncher<Intent> issueVoucherActivityResultLauncher = registerForActivityResult(
      new StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
          return;
        }
      }
  );

  private final ActivityResultLauncher<Intent> addProductsActivityResultLauncher = registerForActivityResult(
      new StartActivityForResult(), result -> {
        if (result.getResultCode() != Activity.RESULT_OK) {
          return;
        }
        Intent intent = new Intent(IssueVoucherInputOrderNumberActivity.this, IssueVoucherActivity.class);
        intent.putExtra(ORDER_NUMBER, orderNumber);
        intent.putExtra(PROGRAM_CODE, chosenProgram.getProgramCode());
        intent.putExtra(MOVEMENT_REASON_CODE, chosenReason.getCode());
        intent.putExtra(SELECTED_PRODUCTS, result.getData().getSerializableExtra(SELECTED_PRODUCTS));
        issueVoucherActivityResultLauncher.launch(intent);
      });

  @NonNull
  private SingleClickButtonListener getNextClickListener() {
    return new SingleClickButtonListener() {
      @Override
      public void onSingleClick(View v) {
        if (validateItemNotBlank() && validateOrderNumber()) {
          Intent intent = new Intent(getApplicationContext(), AddProductsToBulkEntriesActivity.class);
          intent.putExtra(SELECTED_PRODUCTS, (Serializable) Collections.singletonList(""));
          intent.putExtra(IS_FROM_BULK_ISSUE, false);
          intent.putExtra(CHOSEN_PROGRAM_CODE, chosenProgram.getProgramCode());
          addProductsActivityResultLauncher.launch(intent);
        }
      }
    };
  }

  @NonNull
  private SingleClickButtonListener getBackgroundClickListener() {
    return new SingleClickButtonListener() {
      @Override
      public void onSingleClick(View v) {
        validateOrderNumber();
        etOrderNumber.clearFocus();
        hideKeyboard(v);
      }
    };
  }

  private void hideKeyboard(View view) {
    InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(
        Context.INPUT_METHOD_SERVICE);
    if (inputMethodManager != null) {
      inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
  }

  private boolean validateOrderNumber() {
    if (presenter.isOrderNumberExisted(orderNumber)) {
      tilOrderNumber.setError(getResources().getString(R.string.msg_order_number_existed));
      return false;
    }
    tilOrderNumber.setError(null);
    return true;
  }

  private boolean validateItemNotBlank() {
    if (StringUtils.isBlank(orderNumber)) {
      tilOrderNumber.setError(getResources().getString(R.string.alert_order_number_can_not_be_blank));
    }
    if (chosenProgram == null) {
      tilProgram.setError(getResources().getString(R.string.alert_program_can_not_be_blank));
    }
    if (chosenReason == null) {
      tilOrigin.setError(getResources().getString(R.string.alert_movement_reason_can_not_be_blank));
    }
    return !StringUtils.isBlank(orderNumber)
        && chosenReason != null
        && chosenProgram != null;
  }
}
