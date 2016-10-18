package org.openlmis.core.view.holder;

import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.googleAnalytics.TrackerActions;
import org.openlmis.core.googleAnalytics.TrackerCategories;
import org.openlmis.core.model.service.StockService;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.TextStyleUtil;
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

    @InjectView(R.id.ly_expiry_date_warning)
    LinearLayout lyExpiryDateWarning;

    @InjectView(R.id.tv_expiry_date_msg)
    TextView tvExpiryDateMsg;

    @InjectView(R.id.ly_over_stock)
    LinearLayout lyOverStock;

    @InjectView(R.id.ly_low_stock)
    LinearLayout lyLowStock;

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
                    showExpiryDateWithMessage(R.string.msg_expired_date, earliestLotExpiryDate);
                    return;
                }
                if (DateUtil.calculateDateMonthOffset(new Date(), earliestLotExpiryDate) <= 3) {
                    showExpiryDateWithMessage(R.string.msg_expiring_date, earliestLotExpiryDate);
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
                showExpiryDateWithMessage(R.string.msg_expired_date, earliestExpiryDate);
                return;
            }

            if (DateUtil.calculateDateMonthOffset(currentDate, earliestExpiryDate) <= 3) {
                showExpiryDateWithMessage(R.string.msg_expiring_date, earliestExpiryDate);
                return;
            }

            hideExpiryDate();
        }
        hideStockStatus();
    }

    private void showExpiryDateWithMessage(int expiryMsg, Date earliestExpiryDate) {
        if (lyExpiryDateWarning != null) {
            lyExpiryDateWarning.setVisibility(View.VISIBLE);
            tvExpiryDateMsg.setText(context.getResources().getString(expiryMsg, DateUtil.formatDateWithShortMonthAndYear(earliestExpiryDate)));
        }
    }

    private void hideExpiryDate() {
        if (lyExpiryDateWarning != null) {
            lyExpiryDateWarning.setVisibility(View.GONE);
        }
    }

    private void hideStockStatus() {
        if (lyOverStock != null) {
            lyOverStock.setVisibility(View.GONE);
        }

        if (lyLowStock != null) {
            lyLowStock.setVisibility(View.GONE);
        }
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
                tvStockOnHand.setTextColor(context.getResources().getColor(R.color.color_over_stock));
                tvStockOnHand.setTypeface(null, Typeface.NORMAL);
                lyOverStock.setVisibility(View.VISIBLE);
                break;
            case STOCK_ON_HAND_LOW_STOCK:
                tvStockOnHand.setTextColor(context.getResources().getColor(R.color.color_warning_text));
                tvStockOnHand.setTypeface(null, Typeface.NORMAL);
                lyLowStock.setVisibility(View.VISIBLE);
                break;
            case STOCK_ON_HAND_STOCK_OUT:
                tvStockOnHand.setTextColor(context.getResources().getColor(R.color.color_stock_out));
                tvStockOnHand.setTypeface(null, Typeface.BOLD);
                break;
            default:
                stockOnHandBg.setBackgroundResource(R.color.color_white);
                tvStockOnHand.setTextAppearance(context, R.style.Text_Black_Normal);
                tvStockOnHand.setTypeface(null, Typeface.NORMAL);
                hideStockStatus();
                break;
        }
    }

    public interface OnItemViewClickListener {
        void onItemViewClick(InventoryViewModel inventoryViewModel);
    }
}
