package org.openlmis.core.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.R;
import org.openlmis.core.googleAnalytics.ScreenName;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.presenter.NewStockMovementPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.fragment.SimpleSelectDialogFragment;
import org.openlmis.core.view.viewmodel.StockMovementViewModel;
import org.openlmis.core.view.widget.LotListView;
import org.openlmis.core.view.widget.MovementDetailsView;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.util.List;

import lombok.Getter;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_stock_card_new_movement)
public class NewStockMovementActivity extends BaseActivity implements NewStockMovementPresenter.NewStockMovementView, View.OnClickListener {

    MovementDetailsView movementDetailsView;

    LotListView lotListView;

    @InjectView(R.id.btn_complete)
    View btnComplete;

    @InjectView(R.id.btn_cancel)
    TextView tvCancel;

    @InjectPresenter(NewStockMovementPresenter.class)
    NewStockMovementPresenter presenter;

    @Getter
    private String stockName;
    public MovementReasonManager.MovementType movementType;

    private Long stockCardId;

    private List<MovementReasonManager.MovementReason> movementReasons;

    private MovementReasonManager movementReasonManager;

    SimpleSelectDialogFragment reasonsDialog;

    private StockMovementViewModel viewModel;

    private String[] reasonListStr;

    public boolean isKit;

    @Override
    protected ScreenName getScreenName() {
        return ScreenName.StockCardNewMovementScreen;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        movementReasonManager = MovementReasonManager.getInstance();
        stockName = getIntent().getStringExtra(Constants.PARAM_STOCK_NAME);
        movementType = (MovementReasonManager.MovementType) getIntent().getSerializableExtra(Constants.PARAM_MOVEMENT_TYPE);
        stockCardId = getIntent().getLongExtra(Constants.PARAM_STOCK_CARD_ID, 0L);
        isKit = getIntent().getBooleanExtra(Constants.PARAM_IS_KIT, false);
        movementReasons = movementReasonManager.buildReasonListForMovementType(movementType);

        presenter.loadData(stockCardId, movementType);
        viewModel = presenter.getStockMovementViewModel();
        viewModel.setKit(isKit);

        initView();
    }

    private void initView() {
        setTitle(movementType.getDescription() + " " + stockName);
        setUpMovementDetailsView();
        setUpLostListView();
        setUpButtonPanel();
    }

    private void setUpButtonPanel() {
        btnComplete.setOnClickListener(this);
        tvCancel.setOnClickListener(this);
    }

    private void setUpMovementDetailsView() {
        movementDetailsView = (MovementDetailsView) this.findViewById(R.id.view_movement_details);
        movementDetailsView.initMovementDetailsView(presenter, movementType);
        movementDetailsView.setMovementReasonClickListener(getMovementReasonOnClickListener());
        if (isKit) {
            movementDetailsView.setMovementQuantityVisibility(View.VISIBLE);
        }
    }

    private void setUpLostListView() {
        lotListView = (LotListView) this.findViewById(R.id.view_lot_list);
        lotListView.initLotListView(presenter, movementType);
    }


    @NonNull
    private View.OnClickListener getMovementReasonOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                movementDetailsView.setMovementReasonEnable(false);
                reasonListStr = FluentIterable.from(movementReasons).transform(new Function<MovementReasonManager.MovementReason, String>() {
                    @Override
                    public String apply(MovementReasonManager.MovementReason movementReason) {
                        return movementReason.getDescription();
                    }
                }).toArray(String.class);
                reasonsDialog = new SimpleSelectDialogFragment(NewStockMovementActivity.this, new MovementTypeOnClickListener(viewModel), reasonListStr);
                reasonsDialog.setCancelable(false);
                reasonsDialog.show(getFragmentManager(), "");
            }
        };
    }

    public static Intent getIntentToMe(StockMovementsWithLotActivity context, String stockName, MovementReasonManager.MovementType movementType, Long stockCardId, boolean isKit) {
        Intent intent = new Intent(context, NewStockMovementActivity.class);
        intent.putExtra(Constants.PARAM_STOCK_NAME, stockName);
        intent.putExtra(Constants.PARAM_MOVEMENT_TYPE, movementType);
        intent.putExtra(Constants.PARAM_STOCK_CARD_ID, stockCardId);
        intent.putExtra(Constants.PARAM_IS_KIT, isKit);
        return intent;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_complete:
                loading();
                btnComplete.setEnabled(false);
                movementDetailsView.setMovementModelValue();

                if (showErrors()) {
                    if (!isKit) {
                        lotListView.notifyDataChanged();
                    }
                    btnComplete.setEnabled(true);
                    loaded();
                    return;
                }
                presenter.saveStockMovement();
                break;
            case R.id.btn_cancel:
                finish();
                break;
        }
    }

    public void clearErrorAlerts() {
        lotListView.setAlertAddPositiveLotAmountVisibility(View.GONE);
        movementDetailsView.clearTextInputLayoutError();
    }

    protected boolean showErrors() {
        if (StringUtils.isBlank(viewModel.getMovementDate())) {
            showMovementDateEmpty();
            return true;
        }
        if (viewModel.getReason() == null) {
            showMovementReasonEmpty();
            return true;
        }

        if (isKit && checkKitQuantityError()) return true;

        if (StringUtils.isBlank(viewModel.getSignature())) {
            showSignatureErrors(getResources().getString(R.string.msg_empty_signature));
            return true;
        }
        if (!viewModel.validateQuantitiesNotZero()) {
            showQuantityErrors(getResources().getString(R.string.msg_entries_error));
            return true;
        }
        if (!checkSignature(viewModel.getSignature())) {
            showSignatureErrors(getString(R.string.hint_signature_error_message));
            return true;
        }

        return !isKit && (showLotListError() || lotListEmptyError());
    }

    private boolean checkKitQuantityError() {
        MovementReasonManager.MovementType movementType = viewModel.getTypeQuantityMap().keySet().iterator().next();
        if (StringUtils.isBlank(viewModel.getTypeQuantityMap().get(movementType))) {
            showQuantityErrors(getResources().getString(R.string.msg_empty_quantity));
            return true;
        }
        if (quantityIsLargerThanSoh(viewModel.getTypeQuantityMap().get(movementType), movementType)) {
            showQuantityErrors(getResources().getString(R.string.msg_invalid_quantity));
            return true;
        }
        return false;
    }

    private boolean lotListEmptyError() {
        clearErrorAlerts();
        if (this.viewModel.isLotEmpty()) {
            showEmptyLotError();
            return true;
        }
        if (!this.viewModel.movementQuantitiesExist()) {
            showLotQuantityError();
            return true;
        }
        return false;
    }

    private void showLotQuantityError() {
        lotListView.setAlertAddPositiveLotAmountVisibility(View.VISIBLE);
    }

    private boolean checkSignature(String signature) {
        return signature.length() >= 2 && signature.length() <= 5 && signature.matches("\\D+");
    }

    private boolean quantityIsLargerThanSoh(String quantity, MovementReasonManager.MovementType type) {
        return (MovementReasonManager.MovementType.ISSUE.equals(type) || MovementReasonManager.MovementType.NEGATIVE_ADJUST.equals(type)) && Long.parseLong(quantity) > presenter.getStockCard().getStockOnHand();
    }

    private void showEmptyLotError() {
        ToastUtil.show(getResources().getString(R.string.empty_lot_warning));
    }

    @Override
    public void showMovementDateEmpty() {
        clearErrorAlerts();
        movementDetailsView.showMovementDateEmptyError();
    }

    @Override
    public void showMovementReasonEmpty() {
        clearErrorAlerts();
        movementDetailsView.showMovementReasonEmptyError();
    }

    @Override
    public void showQuantityErrors(String errorMsg) {
        clearErrorAlerts();
        movementDetailsView.showMovementQuantityError(errorMsg);
    }

    private void showSignatureErrors(String errorMsg) {
        clearErrorAlerts();
        movementDetailsView.showSignatureError(errorMsg);
    }

    @Override
    public boolean showLotListError() {
        clearErrorAlerts();
        return lotListView.validateLotList();
    }

    @Override
    public void goToStockCard() {
        setResult(RESULT_OK);
        loaded();
        finish();
    }

    class MovementTypeOnClickListener implements AdapterView.OnItemClickListener {
        StockMovementViewModel movementViewModel;

        public MovementTypeOnClickListener(StockMovementViewModel movementViewModel) {
            this.movementViewModel = movementViewModel;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            movementDetailsView.setMovementReasonText(reasonListStr[position]);
            viewModel.setReason(movementReasons.get(position));
            reasonsDialog.dismiss();
            movementDetailsView.setMovementReasonEnable(true);
        }
    }
}