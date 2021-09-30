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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.view.View.OnClickListener;
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
import org.openlmis.core.constant.IntentConstants;
import org.openlmis.core.googleanalytics.ScreenName;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.manager.MovementReasonManager.MovementReason;
import org.openlmis.core.manager.MovementReasonManager.MovementType;
import org.openlmis.core.model.Pod;
import org.openlmis.core.model.Program;
import org.openlmis.core.presenter.IssueVoucherInputOrderNumberPresenter;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.SimpleTextWatcher;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.utils.keyboard.KeyboardUtil;
import org.openlmis.core.view.fragment.SimpleSelectDialogFragment;
import org.openlmis.core.view.widget.SingleClickButtonListener;
import org.roboguice.shaded.goole.common.collect.FluentIterable;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Subscriber;
import rx.Subscription;

@ContentView(R.layout.activity_issue_voucher_input_order_number)
public class IssueVoucherInputOrderNumberActivity extends BaseActivity {

  private final ActivityResultLauncher<Intent> issueVoucherActivityResultLauncher = registerForActivityResult(
      new StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
          return;
        }
      }
  );
  @InjectPresenter(IssueVoucherInputOrderNumberPresenter.class)
  IssueVoucherInputOrderNumberPresenter presenter;
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
  private final ActivityResultLauncher<Intent> addProductsActivityResultLauncher = registerForActivityResult(
      new StartActivityForResult(), result -> {
        if (result.getResultCode() != Activity.RESULT_OK) {
          return;
        }
        Intent intent = new Intent(this, IssueVoucherDraftActivity.class);
        intent.putExtra(IntentConstants.PARAM_ORDER_NUMBER, orderNumber);
        intent.putExtra(IntentConstants.PARAM_CHOSEN_PROGRAM_CODE, chosenProgram.getProgramCode());
        intent.putExtra(IntentConstants.PARAM_MOVEMENT_REASON_CODE, chosenReason.getCode());
        intent.putExtra(SELECTED_PRODUCTS, result.getData().getSerializableExtra(SELECTED_PRODUCTS));
        issueVoucherActivityResultLauncher.launch(intent);
      });
  private List<Program> programItems = new ArrayList<>();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    background.setOnClickListener(getBackgroundClickListener());
    etOrigin.setOnClickListener(getMovementReasonOnClickListener());
    btNext.setOnClickListener(getNextClickListener());
    initForManuallyIssueVoucher();
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

  protected void initProgramData() {
    final Subscription subscription = presenter.loadData().subscribe(getOnProgramsLoadedSubscriber());
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

  private void initForManuallyIssueVoucher() {
    initProgramData();
    addListenerForOrderNumber();
  }

  private void addListenerForOrderNumber() {

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
    etOrderNumber.setOnFocusChangeListener((v, hasFocus) -> {
      if (hasFocus) {
        return;
      }
      if (presenter.isOrderNumberExisted(orderNumber)) {
        tilOrderNumber.setError(getResources().getString(R.string.msg_order_number_existed));
      }
    });
  }

  @NonNull
  private View.OnClickListener getProgramOnClickListener() {
    return new SingleClickButtonListener() {
      @Override
      public void onSingleClick(View view) {
        hideKeyboard();
        String[] programArray = FluentIterable.from(programItems).transform(Program::getProgramName)
            .toArray(String.class);
        Bundle bundle = new Bundle();
        bundle.putStringArray(SimpleSelectDialogFragment.SELECTIONS, programArray);
        SimpleSelectDialogFragment programDialog = new SimpleSelectDialogFragment();
        programDialog.setArguments(bundle);
        programDialog.setItemClickListener((parent, view1, position, id) -> {
          chosenProgram = programItems.get(position);
          updateProgramStatus();
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
        hideKeyboard();
        List<MovementReason> movementReasons = MovementReasonManager.getInstance()
            .buildReasonListForMovementType(MovementType.RECEIVE);
        String[] reasonArray = FluentIterable.from(movementReasons).transform(MovementReason::getDescription)
            .toArray(String.class);
        Bundle bundle = new Bundle();
        bundle.putStringArray(SimpleSelectDialogFragment.SELECTIONS, reasonArray);
        SimpleSelectDialogFragment reasonsDialog = new SimpleSelectDialogFragment();
        reasonsDialog.setArguments(bundle);
        reasonsDialog.setItemClickListener((parent, view1, position, id) -> {
          tilOrigin.setError(null);
          chosenReason = movementReasons.get(position);
          updateMovementReason();
          reasonsDialog.dismiss();
        });
        reasonsDialog.show(getSupportFragmentManager(), "SELECT_REASONS");
      }
    };
  }

  @NonNull
  private SingleClickButtonListener getNextClickListener() {
    return new SingleClickButtonListener() {
      @Override
      public void onSingleClick(View v) {
        hideKeyboard();
        validateForLocal();
      }
    };
  }


  private void validateForLocal() {
    if (validateAll()) {
      Intent intent = new Intent(getApplicationContext(), AddProductsToBulkEntriesActivity.class);
      intent.putExtra(SELECTED_PRODUCTS, (Serializable) Collections.singletonList(""));
      intent.putExtra(IS_FROM_BULK_ISSUE, false);
      intent.putExtra(CHOSEN_PROGRAM_CODE, chosenProgram.getProgramCode());
      addProductsActivityResultLauncher.launch(intent);
    }
  }

  @NonNull
  private OnClickListener getBackgroundClickListener() {
    return v -> hideKeyboard();
  }

  private void hideKeyboard() {
    etOrderNumber.clearFocus();
    KeyboardUtil.hideKeyboard(this);
  }

  private boolean validateAll() {
    if (chosenProgram == null) {
      tilProgram.setError(getResources().getString(R.string.alert_program_can_not_be_blank));
    } else if (presenter.getSameProgramIssueVoucher(chosenProgram) != null) {
      updateProgramStatus();
    }
    if (chosenReason == null) {
      tilOrigin.setError(getResources().getString(R.string.alert_movement_reason_can_not_be_blank));
    }
    return validateOrderNumber()
        && chosenReason != null
        && chosenProgram != null
        && presenter.getSameProgramIssueVoucher(chosenProgram) == null;
  }

  private void updateProgramStatus() {
    if (chosenProgram == null) {
      tilProgram.setError(null);
      etProgram.setText("");
      return;
    }
    etProgram.setText(chosenProgram.getProgramName());
    Pod sameProgramIssueVoucher = presenter.getSameProgramIssueVoucher(chosenProgram);
    if (sameProgramIssueVoucher == null) {
      tilProgram.setError(null);
    } else if (sameProgramIssueVoucher.isLocal()) {
      tilProgram.setError(getString(R.string.msg_has_incomplete_manual_issue_voucher, chosenProgram.getProgramName()));
    } else {
      tilProgram
          .setError(getString(R.string.msg_has_incomplete_electronic_issue_voucher, chosenProgram.getProgramName()));
    }
  }

  private boolean validateOrderNumber() {
    if (StringUtils.isBlank(orderNumber)) {
      tilOrderNumber.setError(getResources().getString(R.string.alert_order_number_can_not_be_blank));
      return false;
    }
    if (presenter.isOrderNumberExisted(orderNumber)) {
      tilOrderNumber.setError(getResources().getString(R.string.msg_order_number_existed));
      return false;
    }
    return true;
  }
}
