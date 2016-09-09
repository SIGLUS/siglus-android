package org.openlmis.core.view.activity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.googleAnalytics.ScreenName;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.manager.NestedRecyclerViewLinearLayoutManager;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.presenter.NewStockMovementPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.LotMovementAdapter;
import org.openlmis.core.view.fragment.SimpleSelectDialogFragment;
import org.openlmis.core.view.listener.MovementDateListener;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.openlmis.core.view.viewmodel.StockMovementViewModel;
import org.openlmis.core.view.widget.AddLotDialogFragment;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.functions.Action1;

@ContentView(R.layout.activity_stock_card_new_movement)
public class StockCardNewMovementActivity extends BaseActivity implements NewStockMovementPresenter.NewStockMovementView, View.OnClickListener {

    @InjectView(R.id.ly_requested_quantity)
    View lyRequestedQuantity;

    @InjectView(R.id.et_movement_date)
    EditText etMovementDate;

    @InjectView(R.id.ly_movement_date)
    TextInputLayout lyMovementDate;

    @InjectView(R.id.et_document_number)
    EditText etDocumentNumber;

    @InjectView(R.id.et_movement_reason)
    EditText etMovementReason;

    @InjectView(R.id.ly_movement_reason)
    TextInputLayout lyMovementReason;

    @InjectView(R.id.et_requested_quantity)
    EditText etRequestedQuantity;

    @InjectView(R.id.et_movement_quantity)
    EditText etMovementQuantity;

    @InjectView(R.id.ly_movement_quantity)
    TextInputLayout lyMovementQuantity;

    @InjectView(R.id.et_movement_signature)
    EditText etMovementSignature;

    @InjectView(R.id.ly_movement_signature)
    TextInputLayout lyMovementSignature;

    @InjectView(R.id.btn_complete)
    View btnComplete;

    @InjectView(R.id.btn_cancel)
    TextView tvCancel;

    @InjectView(R.id.alert_add_lot_amount)
    TextView alertAddLotAmount;

    @InjectView(R.id.action_add_new_lot)
    View actionAddNewLot;

    @InjectView(R.id.lot_list)
    private RecyclerView newLotMovementRecycleView;

    @InjectView(R.id.rv_existing_lot_list)
    private RecyclerView existingLotListView;

    @InjectPresenter(NewStockMovementPresenter.class)
    NewStockMovementPresenter presenter;

    private LotMovementAdapter newLotMovementAdapter;
    private LotMovementAdapter existingLotMovementAdapter;
    private String stockName;
    private MovementReasonManager.MovementType movementType;

    private Long stockCardId;

    StockMovementItem previousMovement;

    private List<MovementReasonManager.MovementReason> movementReasons;

    private MovementReasonManager movementReasonManager;

    SimpleSelectDialogFragment reasonsDialog;

    private StockMovementViewModel stockMovementViewModel;

    private String[] reasonListStr;

    private boolean isKit;
    private Context context;

    private AddLotDialogFragment addLotDialogFragment;

    @Override
    protected ScreenName getScreenName() {
        return ScreenName.StockCardNewMovementScreen;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = this;

        super.onCreate(savedInstanceState);
        movementReasonManager = MovementReasonManager.getInstance();

        stockName = getIntent().getStringExtra(Constants.PARAM_STOCK_NAME);
        movementType = (MovementReasonManager.MovementType) getIntent().getSerializableExtra(Constants.PARAM_MOVEMENT_TYPE);
        stockCardId = getIntent().getLongExtra(Constants.PARAM_STOCK_CARD_ID, 0L);
        isKit = getIntent().getBooleanExtra(Constants.PARAM_IS_KIT, false);
        movementReasons = movementReasonManager.buildReasonListForMovementType(movementType);

        try {
            presenter.loadData(stockCardId, movementType);
            previousMovement = presenter.getPreviousStockMovement();
        } catch (LMISException e) {
            e.printStackTrace();
        }

        stockMovementViewModel = presenter.getStockMovementModel();
        stockMovementViewModel.setKit(isKit);
        initMovementView();
        initExistingLotListView();
        initNewLotListView();
    }

    private void initExistingLotListView() {
        existingLotListView.setLayoutManager(new NestedRecyclerViewLinearLayoutManager(this));
        existingLotMovementAdapter = new LotMovementAdapter(presenter.getExistingLotViewModelsByStockCard(stockCardId));
        existingLotListView.setAdapter(existingLotMovementAdapter);
    }

    private void initNewLotListView() {
        newLotMovementRecycleView.setLayoutManager(new NestedRecyclerViewLinearLayoutManager(this));
        newLotMovementAdapter = new LotMovementAdapter(presenter.getStockMovementModel().getNewLotMovementViewModelList(), previousMovement.getStockCard().getProduct().getProductNameWithCodeAndStrength());
        newLotMovementRecycleView.setAdapter(newLotMovementAdapter);
    }

    private void refreshNewLotList() {
        newLotMovementAdapter.notifyDataSetChanged();
    }

    private void initMovementView() {
        setTitle(movementType.getDescription() + " " + stockName);

        if (!movementType.equals(MovementReasonManager.MovementType.ISSUE)) {
            lyRequestedQuantity.setVisibility(View.GONE);
        }

        if (!isKit) {
            if (MovementReasonManager.MovementType.RECEIVE.equals(movementType)
                    || MovementReasonManager.MovementType.POSITIVE_ADJUST.equals(movementType)) {
                actionAddNewLot.setVisibility(View.VISIBLE);
            }
            lyMovementQuantity.setVisibility(View.GONE);
        }

        if (MovementReasonManager.MovementType.RECEIVE.equals(movementType)
                || MovementReasonManager.MovementType.POSITIVE_ADJUST.equals(movementType)) {
            lyMovementReason.setHint(getResources().getString(R.string.hint_movement_reason_receive));
        }

        btnComplete.setOnClickListener(this);
        tvCancel.setOnClickListener(this);

        etMovementDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog(presenter.getStockMovementModel(), previousMovement.getMovementDate());
            }
        });
        etMovementDate.setKeyListener(null);

        etMovementReason.setOnClickListener(getMovementReasonOnClickListener());
        etMovementReason.setKeyListener(null);

        actionAddNewLot.setOnClickListener(getAddNewLotOnClickListener());
    }

    @NonNull
    private View.OnClickListener getAddNewLotOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionAddNewLot.setEnabled(false);
                addLotDialogFragment = new AddLotDialogFragment();
                addLotDialogFragment.setListener(getAddNewLotDialogOnClickListener());
                addLotDialogFragment.show(getFragmentManager(), "");
            }
        };
    }

    @NonNull
    private View.OnClickListener getAddNewLotDialogOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btn_complete:
                        if (addLotDialogFragment.validate() && !addLotDialogFragment.hasIdenticalLot(getLotNumbers())) {
                            presenter.addLotMovement(new LotMovementViewModel(addLotDialogFragment.getLotNumber(),
                                    addLotDialogFragment.getExpiryDate(), movementType))
                                    .subscribe(new Action1<List<LotMovementViewModel>>() {
                                @Override
                                public void call(List<LotMovementViewModel> lotMovementViewModels) {
                                    refreshNewLotList();
                                }
                            });
                            addLotDialogFragment.dismiss();
                        }
                        actionAddNewLot.setEnabled(true);
                        break;
                    case R.id.btn_cancel:
                        addLotDialogFragment.dismiss();
                        actionAddNewLot.setEnabled(true);
                        break;
                }
            }
        };
    }

    @NonNull
    private List<String> getLotNumbers() {
        final List<String> existingLots = new ArrayList<>();
        existingLots.addAll(FluentIterable.from(stockMovementViewModel.getNewLotMovementViewModelList()).transform(new Function<LotMovementViewModel, String>() {
            @Override
            public String apply(LotMovementViewModel lotMovementViewModel) {
                return lotMovementViewModel.getLotNumber();
            }
        }).toList());
        existingLots.addAll(FluentIterable.from((stockMovementViewModel.getExistingLotMovementViewModelList())).transform(new Function<LotMovementViewModel, String>() {
            @Override
            public String apply(LotMovementViewModel lotMovementViewModel) {
                return lotMovementViewModel.getLotNumber();
            }
        }).toList());
        return existingLots;
    }

    @NonNull
    private View.OnClickListener getMovementReasonOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reasonListStr = FluentIterable.from(movementReasons).transform(new Function<MovementReasonManager.MovementReason, String>() {
                    @Override
                    public String apply(MovementReasonManager.MovementReason movementReason) {
                        return movementReason.getDescription();
                    }
                }).toArray(String.class);
                reasonsDialog = new SimpleSelectDialogFragment(context, new MovementTypeOnClickListener(stockMovementViewModel), reasonListStr);
                reasonsDialog.show(getFragmentManager(), "");
            }
        };
    }

    public static Intent getIntentToMe(StockMovementsActivityNew context, String stockName, MovementReasonManager.MovementType movementType, Long stockCardId, boolean isKit) {
        Intent intent = new Intent(context, StockCardNewMovementActivity.class);
        intent.putExtra(Constants.PARAM_STOCK_NAME, stockName);
        intent.putExtra(Constants.PARAM_MOVEMENT_TYPE, movementType);
        intent.putExtra(Constants.PARAM_STOCK_CARD_ID, stockCardId);
        intent.putExtra(Constants.PARAM_IS_KIT, isKit);
        return intent;
    }

    private void showDatePickerDialog(StockMovementViewModel model, Date previousMovementDate) {
        final Calendar today = GregorianCalendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(this, DatePickerDialog.BUTTON_NEUTRAL,
                new MovementDateListener(model, previousMovementDate, etMovementDate),
                today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_complete:
                stockMovementViewModel.setMovementDate(etMovementDate.getText().toString());
                stockMovementViewModel.setDocumentNo(etDocumentNumber.getText().toString());
                stockMovementViewModel.setRequested(etRequestedQuantity.getText().toString());
                HashMap<MovementReasonManager.MovementType, String> quantityMap = new HashMap<>();
                quantityMap.put(movementType, etMovementQuantity.getText().toString());
                stockMovementViewModel.setTypeQuantityMap(quantityMap);
                stockMovementViewModel.setSignature(etMovementSignature.getText().toString());
                if (showErrors(stockMovementViewModel)) {
                    existingLotMovementAdapter.notifyDataSetChanged();
                    newLotMovementAdapter.notifyDataSetChanged();
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
        alertAddLotAmount.setVisibility(View.GONE);
        lyMovementDate.setErrorEnabled(false);
        lyMovementReason.setErrorEnabled(false);
        lyMovementQuantity.setErrorEnabled(false);
        lyMovementSignature.setErrorEnabled(false);
    }

    protected boolean showErrors(StockMovementViewModel stockMovementViewModel) {
        MovementReasonManager.MovementType movementType = stockMovementViewModel.getTypeQuantityMap().keySet().iterator().next();
        if (StringUtils.isBlank(stockMovementViewModel.getMovementDate())) {
            showMovementDateEmpty();
            return true;
        }
        if (stockMovementViewModel.getReason() == null) {
            showMovementReasonEmpty();
            return true;
        }

        if (isKit && checkKitQuantityError(stockMovementViewModel, movementType)) return true;

        if (StringUtils.isBlank(stockMovementViewModel.getSignature())) {
            showSignatureErrors(getResources().getString(R.string.msg_empty_signature));
            return true;
        }
        if (!stockMovementViewModel.validateQuantitiesNotZero()) {
            showQuantityErrors(getResources().getString(R.string.msg_entries_error));
            return true;
        }
        if (!checkSignature(stockMovementViewModel.getSignature())) {
            showSignatureErrors(getString(R.string.hint_signature_error_message));
            return true;
        }

        return !isKit && (showLotListError() || lotListEmptyError());
    }

    private boolean checkKitQuantityError(StockMovementViewModel stockMovementViewModel, MovementReasonManager.MovementType movementType) {
        if (StringUtils.isBlank(stockMovementViewModel.getTypeQuantityMap().get(movementType))) {
            showQuantityErrors(getResources().getString(R.string.msg_empty_quantity));
            return true;
        }
        if (quantityIsLargerThanSoh(stockMovementViewModel.getTypeQuantityMap().get(movementType), movementType)) {
            showQuantityErrors(getResources().getString(R.string.msg_invalid_quantity));
            return true;
        }
        return false;
    }

    private boolean lotListEmptyError() {
        clearErrorAlerts();
        if (this.stockMovementViewModel.isLotEmpty()) {
            showEmptyLotError();
            return true;
        }
        if (!this.stockMovementViewModel.movementQuantitiesExist()) {
            showLotQuantityError();
            return true;
        }
        return false;
    }

    private void showLotQuantityError() {
        clearErrorAlerts();
        alertAddLotAmount.setVisibility(View.VISIBLE);
    }

    private boolean checkSignature(String signature) {
        return signature.length() >= 2 && signature.length() <= 5 && signature.matches("\\D+");
    }

    private boolean quantityIsLargerThanSoh(String quantity, MovementReasonManager.MovementType type) {
        return (MovementReasonManager.MovementType.ISSUE.equals(type) || MovementReasonManager.MovementType.NEGATIVE_ADJUST.equals(type)) && Long.parseLong(quantity) > previousMovement.getStockOnHand();
    }

    private void showEmptyLotError() {
        clearErrorAlerts();
        ToastUtil.show(getResources().getString(R.string.empty_lot_warning));
    }

    @Override
    public void showMovementDateEmpty() {
        clearErrorAlerts();
        lyMovementDate.setError(getResources().getString(R.string.msg_empty_movement_date));
        etMovementDate.getBackground().setColorFilter(getResources().getColor(R.color.color_red), PorterDuff.Mode.SRC_ATOP);
    }

    @Override
    public void showMovementReasonEmpty() {
        clearErrorAlerts();
        lyMovementReason.setError(getResources().getString(R.string.msg_empty_movement_reason));
        etMovementReason.getBackground().setColorFilter(getResources().getColor(R.color.color_red), PorterDuff.Mode.SRC_ATOP);
    }

    @Override
    public void showQuantityErrors(String errorMsg) {
        clearErrorAlerts();
        lyMovementQuantity.setError(errorMsg);
        etMovementQuantity.getBackground().setColorFilter(getResources().getColor(R.color.color_red), PorterDuff.Mode.SRC_ATOP);
    }

    private void showSignatureErrors(String string) {
        clearErrorAlerts();
        lyMovementSignature.setError(string);
        etMovementSignature.getBackground().setColorFilter(getResources().getColor(R.color.color_red), PorterDuff.Mode.SRC_ATOP);
    }


    @Override
    public boolean showLotListError() {
        clearErrorAlerts();
        int position1 = existingLotMovementAdapter.validateExisting(movementType);
        if (position1 >= 0) {
            existingLotListView.scrollToPosition(position1);
            return true;
        }
        int position2 = newLotMovementAdapter.validateAll();
        if (position2 >= 0) {
            newLotMovementRecycleView.scrollToPosition(position2);
            return true;
        }
        return false;
    }

    @Override
    public void goToStockCard() {
        setResult(RESULT_OK);
        finish();
    }

    class MovementTypeOnClickListener implements AdapterView.OnItemClickListener {
        StockMovementViewModel movementViewModel;

        public MovementTypeOnClickListener(StockMovementViewModel movementViewModel) {
            this.movementViewModel = movementViewModel;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            etMovementReason.setText(reasonListStr[position]);
            stockMovementViewModel.setReason(movementReasons.get(position));
            reasonsDialog.dismiss();
        }
    }
}