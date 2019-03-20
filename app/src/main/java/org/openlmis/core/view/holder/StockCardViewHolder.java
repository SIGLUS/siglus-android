package org.openlmis.core.view.holder;

import android.graphics.Typeface;
import android.support.annotation.ColorRes;
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

    @InjectView(R.id.tv_product_name)
    TextView tvProductName;
    @InjectView(R.id.tv_product_unit)
    TextView tvProductUnit;
    @InjectView(R.id.tv_stock_on_hand)
    TextView tvStockOnHand;
    @InjectView(R.id.vg_stock_on_hand_bg)
    View stockOnHandBg;

    @InjectView(R.id.ly_expiry_date_warning)
    LinearLayout lyExpiryDateWarning;

    @InjectView(R.id.tv_expiry_date_msg)
    TextView tvExpiryDateMsg;

    @InjectView(R.id.ly_stock_status)
    LinearLayout lyStockStatus;

    @InjectView(R.id.tv_stock_status)
    TextView tvStockStatus;

    protected StockService stockService;
    private OnItemViewClickListener listener;

    public enum StockOnHandStatus {

        REGULAR_STOCK("regularStock", R.string.Regular_stock, R.color.color_regular_stock, R.color.color_stock_status),
        LOW_STOCK("lowStock", R.string.Low_stock, R.color.color_low_stock, R.color.color_stock_status),
        STOCK_OUT("stockOut", R.string.Stock_out, R.color.color_stock_out, R.color.color_stock_status),
        OVER_STOCK("overStock",R.string.Overstock, R.color.color_over_stock, R.color.color_stock_status);

        private String messageKey;
        private int description;
        private @ColorRes int bgColor;
        private @ColorRes int color;

        StockOnHandStatus(String key, int desc, @ColorRes int bgColor, @ColorRes int color) {
            this.messageKey = key;
            this.description = desc;
            this.bgColor = bgColor;
            this.color = color;
        }

        public String getMessageKey() {
            return messageKey;
        }

        public int getDescription() {
            return description;
        }

        public @ColorRes int getColor() {
            return color;
        }

        public @ColorRes int getBgColor() {
            return bgColor;
        }
    }

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
        Date earliestLotExpiryDate = inventoryViewModel.getStockCard().getEarliestLotExpiryDate();

        if (earliestLotExpiryDate != null) {
            if (earliestLotExpiryDate.before(new Date(LMISApp.getInstance().getCurrentTimeMillis()))) {
                showExpiryDateWithMessage(R.string.msg_expired_date, earliestLotExpiryDate);
                return;
            }
            if (DateUtil.calculateDateMonthOffset(new Date(LMISApp.getInstance().getCurrentTimeMillis()), earliestLotExpiryDate) <= 3) {
                showExpiryDateWithMessage(R.string.msg_expiring_date, earliestLotExpiryDate);
                return;
            }
        }
        hideExpiryDate();
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

        StockOnHandStatus stockOnHandStatus = viewModel.getStockOnHandLevel();
        tvStockStatus.setText(context.getResources().getString(stockOnHandStatus.description));
        tvStockStatus.setTextColor(context.getResources().getColor(stockOnHandStatus.getColor()));
        tvStockStatus.setBackgroundColor(context.getResources().getColor(stockOnHandStatus.getBgColor()));

        switch (stockOnHandStatus) {
            case OVER_STOCK:
                tvStockOnHand.setTextColor(context.getResources().getColor(R.color.color_over_stock));
                tvStockOnHand.setTypeface(null, Typeface.NORMAL);
                break;
            case LOW_STOCK:
                tvStockOnHand.setTextColor(context.getResources().getColor(R.color.color_low_stock));
                tvStockOnHand.setTypeface(null, Typeface.NORMAL);
                break;
            case STOCK_OUT:
                tvStockOnHand.setTextColor(context.getResources().getColor(R.color.color_stock_out));
                tvStockOnHand.setTypeface(null, Typeface.BOLD);
                break;
            default:
                stockOnHandBg.setBackgroundResource(R.color.color_white);
                tvStockOnHand.setTextAppearance(context, R.style.Text_Black_Normal);
                tvStockOnHand.setTypeface(null, Typeface.NORMAL);
                break;
        }
    }

    public interface OnItemViewClickListener {
        void onItemViewClick(InventoryViewModel inventoryViewModel);
    }
}
