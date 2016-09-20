package org.openlmis.core.view.holder;

import android.app.DialogFragment;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewStub;
import android.widget.TextView;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.googleAnalytics.TrackerActions;
import org.openlmis.core.googleAnalytics.TrackerCategories;
import org.openlmis.core.model.service.StockService;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.TextStyleUtil;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.activity.BaseActivity;
import org.openlmis.core.view.fragment.SimpleDialogFragment;
import org.openlmis.core.view.viewmodel.InventoryViewModel;

import java.util.Date;

import roboguice.RoboGuice;
import roboguice.inject.InjectView;

public class StockCardViewHolder extends BaseViewHolder {

    @InjectView(R.id.product_name)
    TextView tvProductName;
    @InjectView(R.id.product_unit)
    TextView tvProductUnit;
    @InjectView(R.id.tv_stock_on_hand)
    TextView tvStockOnHand;
    @InjectView(R.id.vg_stock_on_hand_bg)
    View stockOnHandBg;

    @InjectView(R.id.vs_warning)
    ViewStub vsWarning;
    View ivWarning;

    @InjectView(R.id.vs_expiry_date_warning)
    ViewStub vsExpiryDateWarning;
    View ivExpiryDateWarning;

    protected StockService stockService;
    private OnItemViewClickListener listener;

    public static final int STOCK_ON_HAND_NORMAL = 1;
    public static final int STOCK_ON_HAND_LOW_STOCK = 2;
    public static final int STOCK_ON_HAND_OVER_STOCK = 3;
    public static final int STOCK_ON_HAND_STOCK_OUT = 4;

    public StockCardViewHolder(View itemView, OnItemViewClickListener listener) {
        super(itemView);
        this.listener = listener;
        this.stockService = RoboGuice.getInjector(context).getInstance(StockService.class);
    }

    public void populate(final InventoryViewModel inventoryViewModel, String queryKeyWord) {
        setListener(inventoryViewModel);
        inflateData(inventoryViewModel, queryKeyWord);
    }

    protected void inflateData(InventoryViewModel inventoryViewModel, String queryKeyWord) {
        tvStockOnHand.setText(inventoryViewModel.getStockOnHand() + "");
        tvProductName.setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, inventoryViewModel.getStyledName()));
        tvProductUnit.setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, inventoryViewModel.getStyledUnit()));

        initExpiryDateWarning(inventoryViewModel);

        initStockOnHandWarning(inventoryViewModel);
    }

    private void initExpiryDateWarning(InventoryViewModel inventoryViewModel) {
        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_lot_management)) {
            Date earliestLotExpiryDate = inventoryViewModel.getStockCard().getEarliestLotExpiryDate();

            if (earliestLotExpiryDate != null) {
                if (earliestLotExpiryDate.before(new Date())) {
                    showExpiryDateWithMessage(context.getString(R.string.msg_expired_warning));
                    return;
                }
                if (DateUtil.calculateDateMonthOffset(new Date(), earliestLotExpiryDate) <= 3) {
                    showExpiryDateWithMessage(context.getString(R.string.msg_expiry_warning));
                    return;
                }
            }
            hideExpiryDate();
        } else {
            String earliestExpiryDateString = inventoryViewModel.getStockCard().getEarliestExpireDate();
            if (TextUtils.isEmpty(earliestExpiryDateString)) {
                hideExpiryDate();
                return;
            }

            Date earliestExpiryDate = DateUtil.parseString(earliestExpiryDateString, DateUtil.SIMPLE_DATE_FORMAT);
            Date currentDate = new Date(LMISApp.getInstance().getCurrentTimeMillis());

            if (DateUtil.calculateDateMonthOffset(earliestExpiryDate, currentDate) > 0) {
                showExpiryDateWithMessage(context.getString(R.string.msg_expired_warning));
                return;
            }

            if (DateUtil.calculateDateMonthOffset(currentDate, earliestExpiryDate) <= 3) {
                showExpiryDateWithMessage(context.getString(R.string.msg_expiry_warning));
                return;
            }

            hideExpiryDate();
        }
    }

    private void showExpiryDateWithMessage(String expiryMsg) {
        if (ivExpiryDateWarning != null) {
            ivExpiryDateWarning.setVisibility(View.VISIBLE);
        } else {
            ivExpiryDateWarning = vsExpiryDateWarning.inflate();
        }
        initWarningLister(expiryMsg);
    }

    private void hideExpiryDate() {
        if (ivExpiryDateWarning != null) {
            ivExpiryDateWarning.setVisibility(View.GONE);
        }
    }

    protected void initWarningLister(final String expiryMsg) {
        ivExpiryDateWarning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialogFragment = SimpleDialogFragment.newInstance(null,
                        expiryMsg,
                        context.getString(R.string.btn_ok));
                dialogFragment.show(((BaseActivity) context).getFragmentManager(), "expiryDateWarningDialog");
            }
        });
    }

    private void setListener(final InventoryViewModel inventoryViewModel) {
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    LMISApp.getInstance().trackEvent(TrackerCategories.StockMovement, TrackerActions.SelectStockCard);
                    listener.onItemViewClick(inventoryViewModel);
                }
            }
        });
    }

    private void initStockOnHandWarning(final InventoryViewModel viewModel) {

        int stockOnHandLevel = viewModel.getStockOnHandLevel();

        switch (stockOnHandLevel) {
            case STOCK_ON_HAND_OVER_STOCK:
                stockOnHandBg.setBackgroundResource(R.color.color_over_stock);
                tvStockOnHand.setTextColor(context.getResources().getColor(R.color.color_white));
                showWarning(context.getString(R.string.msg_over_stock_warning));
                break;
            case STOCK_ON_HAND_LOW_STOCK:
                stockOnHandBg.setBackgroundResource(R.color.color_warning);
                tvStockOnHand.setTextColor(context.getResources().getColor(R.color.color_black));
                showWarning(context.getString(R.string.msg_low_stock_warning));
                break;
            case STOCK_ON_HAND_STOCK_OUT:
                stockOnHandBg.setBackgroundResource(R.color.color_stock_out);
                tvStockOnHand.setTextColor(context.getResources().getColor(R.color.color_black));
                showWarning(context.getString(R.string.msg_stock_out_warning));
                break;
            default:
                stockOnHandBg.setBackgroundResource(R.color.color_primary_50);
                tvStockOnHand.setTextColor(context.getResources().getColor(R.color.color_black));
                if (ivWarning != null) {
                    ivWarning.setVisibility(View.GONE);
                }
                break;
        }
    }

    private void showWarning(final String warningMsg) {
        if (ivWarning != null) {
            ivWarning.setVisibility(View.VISIBLE);
        } else {
            ivWarning = vsWarning.inflate();
        }
        ivWarning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToastUtil.showCustomToast(warningMsg);
            }
        });
    }

    public interface OnItemViewClickListener {
        void onItemViewClick(InventoryViewModel inventoryViewModel);
    }
}
