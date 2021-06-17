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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import org.openlmis.core.R;
import org.openlmis.core.googleanalytics.ScreenName;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.presenter.NewStockMovementPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.fragment.SimpleSelectDialogFragment;
import org.openlmis.core.view.widget.MovementDetailsView;
import org.openlmis.core.view.widget.NewMovementLotListView;
import org.openlmis.core.view.widget.SingleClickButtonListener;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_new_stock_movement)
public class NewStockMovementActivity extends BaseActivity implements
    NewStockMovementPresenter.NewStockMovementView {

  @InjectView(R.id.view_movement_details)
  MovementDetailsView movementDetailsView;

  @InjectView(R.id.ly_lot_area)
  ViewGroup lyLotArea;

  @InjectView(R.id.view_lot_list)
  NewMovementLotListView newMovementLotListView;

  @InjectView(R.id.btn_complete)
  View btnComplete;

  @InjectView(R.id.btn_cancel)
  TextView tvCancel;

  @InjectPresenter(NewStockMovementPresenter.class)
  NewStockMovementPresenter presenter;

  @Override
  protected ScreenName getScreenName() {
    return ScreenName.STOCK_CARD_NEW_MOVEMENT_SCREEN;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    String stockName = getIntent().getStringExtra(Constants.PARAM_STOCK_NAME);
    MovementReasonManager.MovementType movementType = (MovementReasonManager.MovementType)
        getIntent().getSerializableExtra(Constants.PARAM_MOVEMENT_TYPE);
    Long stockCardId = getIntent().getLongExtra(Constants.PARAM_STOCK_CARD_ID, 0L);
    boolean isKit = getIntent().getBooleanExtra(Constants.PARAM_IS_KIT, false);

    presenter.loadData(stockCardId, movementType, isKit);

    setTitle(movementType.getDescription() + " " + stockName);
    initView();
  }

  private void initView() {
    setUpMovementDetailsView();
    setUpLostListView();
    setUpButtonPanel();
  }

  @Override
  protected void onStart() {
    super.onStart();
    if (!presenter.shouldLoadKitMovementPage()) {
      ToastUtil.showInCenter(R.string.msg_stock_movement_is_not_ready);
      finish();
    }
  }

  private void setUpButtonPanel() {
    SingleClickButtonListener singleClickButtonListener = getSingleClickButtonListener();
    btnComplete.setOnClickListener(singleClickButtonListener);
    tvCancel.setOnClickListener(singleClickButtonListener);
  }

  private void setUpMovementDetailsView() {
    movementDetailsView.initMovementDetailsView(presenter);
    movementDetailsView.setMovementReasonClickListener(getMovementReasonOnClickListener());
    if (presenter.isKit()) {
      movementDetailsView.setMovementQuantityVisibility(View.VISIBLE);
    }
  }

  private void setUpLostListView() {
    if (!presenter.isKit()) {
      newMovementLotListView.initLotListView(presenter.getViewModel());
    } else {
      lyLotArea.setVisibility(View.GONE);
    }
  }

  @NonNull
  private View.OnClickListener getMovementReasonOnClickListener() {
    return new SingleClickButtonListener() {
      @Override
      public void onSingleClick(View view) {
        Bundle bundle = new Bundle();
        bundle.putStringArray(SimpleSelectDialogFragment.SELECTIONS,
            presenter.getMovementReasonDescriptionList());
        SimpleSelectDialogFragment reasonsDialog = new SimpleSelectDialogFragment();
        reasonsDialog.setArguments(bundle);
        reasonsDialog
            .setMovementTypeOnClickListener(new MovementTypeOnClickListener(reasonsDialog));
        reasonsDialog.show(getSupportFragmentManager(), "SELECT_REASONS");
      }
    };
  }

  public static Intent getIntentToMe(StockMovementsWithLotActivity context, String stockName,
      MovementReasonManager.MovementType movementType, Long stockCardId, boolean isKit) {
    Intent intent = new Intent(context, NewStockMovementActivity.class);
    intent.putExtra(Constants.PARAM_STOCK_NAME, stockName);
    intent.putExtra(Constants.PARAM_MOVEMENT_TYPE, movementType);
    intent.putExtra(Constants.PARAM_STOCK_CARD_ID, stockCardId);
    intent.putExtra(Constants.PARAM_IS_KIT, isKit);
    return intent;
  }

  public SingleClickButtonListener getSingleClickButtonListener() {
    return new SingleClickButtonListener() {
      @Override
      public void onSingleClick(View v) {
        switch (v.getId()) {
          case R.id.btn_complete:
            loading();
            btnComplete.setEnabled(false);
            movementDetailsView.setMovementModelValue();

            if (validate()) {
              presenter.saveStockMovement();
            } else {
              loaded();
              btnComplete.setEnabled(true);
            }
            break;
          case R.id.btn_cancel:
            finish();
            break;
          default:
            // do nothing
        }
      }
    };
  }

  private boolean validate() {
    boolean isValid = presenter.isKit() || newMovementLotListView.validate();
    isValid = movementDetailsView.validate() && isValid;
    return isValid;
  }

  @Override
  public void clearErrorAlerts() {
    newMovementLotListView.setAlertAddPositiveLotAmountVisibility(View.GONE);
    movementDetailsView.clearTextInputLayoutError();
  }

  @Override
  public void showQuantityErrors(String errorMsg) {
    clearErrorAlerts();
    movementDetailsView.showMovementQuantityError(errorMsg);
  }

  @Override
  public void goToStockCard() {
    setResult(RESULT_OK);
    loaded();
    finish();
  }

  class MovementTypeOnClickListener implements AdapterView.OnItemClickListener {

    private final SimpleSelectDialogFragment reasonsDialog;

    public MovementTypeOnClickListener(SimpleSelectDialogFragment reasonsDialog) {
      this.reasonsDialog = reasonsDialog;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
      movementDetailsView
          .setMovementReasonText(presenter.getMovementReasonDescriptionList()[position]);
      presenter.getViewModel().setReason(presenter.getMovementReasons().get(position));
      reasonsDialog.dismiss();
    }
  }
}