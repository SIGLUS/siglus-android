package org.openlmis.core.view.holder;

import android.graphics.Typeface;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.enums.StockOnHandStatus;
import org.openlmis.core.googleAnalytics.TrackerActions;
import org.openlmis.core.googleAnalytics.TrackerCategories;
import org.openlmis.core.model.service.StockService;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.TextStyleUtil;
import org.openlmis.core.view.viewmodel.InventoryViewModel;

import java.util.Calendar;
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

    @InjectView(R.id.tv_stock_status)
    TextView tvStockStatus;

    protected StockService stockService;
    private OnItemViewClickListener listener;

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
            Calendar earliestLotExpiryDateAddTwoDay= Calendar.getInstance();
            earliestLotExpiryDateAddTwoDay.setTime(earliestLotExpiryDate);
            earliestLotExpiryDateAddTwoDay.add(Calendar.DATE,2);
           if (earliestLotExpiryDateAddTwoDay.getTime().compareTo(new Date(LMISApp.getInstance().getCurrentTimeMillis()))<=0) {
                showExpiryDateWithMessage(R.string.msg_expired_date,earliestLotExpiryDate);
                return;
            }
            if (DateUtil.calculateDateMonthOffset(new Date(LMISApp.getInstance().getCurrentTimeMillis()), earliestLotExpiryDateAddTwoDay.getTime()) <= 3) {
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
                    LMISApp.getInstance().trackEvent(TrackerCategories.StockMovement, TrackerActions.SELECT_STOCK_CARD);
                    listener.onItemViewClick(inventoryViewModel);
                }
            }
        });
    }

    private void initStockOnHandWarning(final InventoryViewModel viewModel) {

        StockOnHandStatus stockOnHandStatus = StockOnHandStatus.calculateStockOnHandLevel(viewModel.getStockCard());
        tvStockStatus.setText(context.getResources().getString(stockOnHandStatus.getDescription()));
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
