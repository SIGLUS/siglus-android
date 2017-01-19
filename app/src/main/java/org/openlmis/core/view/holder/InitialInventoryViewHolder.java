package org.openlmis.core.view.holder;

import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.utils.TextStyleUtil;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.widget.InitialInventoryLotListView;
import org.openlmis.core.view.widget.SingleClickButtonListener;

import roboguice.inject.InjectView;


public class InitialInventoryViewHolder extends BaseViewHolder {

    @InjectView(R.id.tv_product_name)
    TextView productName;

    @InjectView(R.id.tv_product_unit)
    TextView productUnit;

    @InjectView(R.id.checkbox)
    CheckBox checkBox;

    @InjectView(R.id.action_view_history)
    TextView tvHistoryAction;

    @InjectView(R.id.touchArea_checkbox)
    LinearLayout taCheckbox;

    @InjectView(R.id.view_lot_list)
    InitialInventoryLotListView lotListView;

    private InventoryViewModel viewModel;

    public InitialInventoryViewHolder(View itemView) {
        super(itemView);
        initView();
    }

    private void initView() {
        taCheckbox.setOnClickListener(new SingleClickButtonListener() {
            @Override
            public void onSingleClick(View v) {
                checkBox.setChecked(!checkBox.isChecked());
            }
        });
    }

    public void populate(final InventoryViewModel inventoryViewModel, String queryKeyWord, ViewHistoryListener listener) {
        this.viewModel = inventoryViewModel;
        setUpLotListView();
        resetCheckBox();
        setUpCheckBox();

        checkBox.setChecked(viewModel.isChecked());

        productName.setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, viewModel.getStyledName()));
        productUnit.setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, viewModel.getStyleType()));

        initHistoryView(listener);
    }

    public void setUpLotListView() {
        lotListView.setUpdateCheckBoxListener(new InitialInventoryLotListView.UpdateCheckBoxListener() {
            @Override
            public void updateCheckBox() {
                checkBox.setEnabled(true);
                if (viewModel.getNewLotMovementViewModelList().isEmpty()) {
                    checkBox.setChecked(false);
                }
            }
        });
        lotListView.initLotListView(viewModel);
    }

    private void resetCheckBox() {
        checkBox.setEnabled(true);
        checkBox.setOnCheckedChangeListener(null);
        checkBox.setChecked(false);
        lotListView.setVisibility(View.GONE);
    }

    protected void setUpCheckBox() {
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkBox.setEnabled(false);
                checkedChangeAction(isChecked);
            }
        });
    }

    private void checkedChangeAction(boolean isChecked) {
        if (isChecked && !viewModel.getProduct().isArchived()) {
            if (!viewModel.getNewLotMovementViewModelList().isEmpty()) {
                showAddNewLotPanel(View.VISIBLE);
                checkBox.setEnabled(true);
                viewModel.setChecked(true);
            } else if (lotListView.showAddLotDialogFragment()) {
                showAddNewLotPanel(View.VISIBLE);
                viewModel.setChecked(true);
            } else {
                checkBox.setEnabled(true);
                checkBox.setChecked(false);
            }
        } else {
            checkBox.setEnabled(true);
            showAddNewLotPanel(View.GONE);
            viewModel.getNewLotMovementViewModelList().clear();
            lotListView.refreshNewLotList();
            viewModel.setChecked(isChecked);
        }
    }

    private void initHistoryView(final ViewHistoryListener listener) {
        tvHistoryAction.setVisibility(viewModel.getProduct().isArchived() ? View.VISIBLE : View.GONE);
        tvHistoryAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.viewHistory(viewModel.getStockCard());
                }
            }
        });
    }

    public void showAddNewLotPanel(int visible) {
        lotListView.setVisibility(visible);
    }

    public interface ViewHistoryListener {
        void viewHistory(StockCard stockCard);
    }
}
