package org.openlmis.core.view.holder;

import android.view.View;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.utils.TextStyleUtil;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.viewmodel.StockCardViewModel;

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
    @InjectView(R.id.iv_warning)
    View iv_warning;

    protected StockRepository stockRepository;
    private OnItemViewClickListener listener;

    protected static final int STOCK_ON_HAND_NORMAL = 1;
    protected static final int STOCK_ON_HAND_LOW_STOCK = 2;
    protected static final int STOCK_ON_HAND_STOCK_OUT = 3;

    public StockCardViewHolder(View itemView, OnItemViewClickListener listener) {
        super(itemView);
        this.listener = listener;
        this.stockRepository = RoboGuice.getInjector(context).getInstance(StockRepository.class);
    }

    public void populate(final StockCardViewModel stockCardViewModel, String queryKeyWord) {

        tvProductName.setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, stockCardViewModel.getStyledName()));
        tvProductUnit.setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, stockCardViewModel.getStyledUnit()));

        initStockOnHand(stockCardViewModel);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemViewClick(stockCardViewModel);
                }
            }
        });
    }

    private void initStockOnHand(final StockCardViewModel stockCard) {
        tvStockOnHand.setText(stockCard.getStockOnHand() + "");

        int stockOnHandLevel = getStockOnHandLevel(stockCard);
        String warningMsg = null;

        switch (stockOnHandLevel) {
            case STOCK_ON_HAND_LOW_STOCK:
                stockOnHandBg.setBackgroundResource(R.color.color_warning);
                warningMsg = context.getString(R.string.msg_low_stock_warning);
                iv_warning.setVisibility(View.VISIBLE);
                break;
            case STOCK_ON_HAND_STOCK_OUT:
                stockOnHandBg.setBackgroundResource(R.color.color_stock_out);
                warningMsg = context.getString(R.string.msg_stock_out_warning);
                iv_warning.setVisibility(View.VISIBLE);
                break;
            default:
                stockOnHandBg.setBackgroundResource(R.color.color_primary_50);
                iv_warning.setVisibility(View.GONE);
                break;
        }
        final String finalWarningMsg = warningMsg;
        iv_warning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToastUtil.showCustomToast(finalWarningMsg);
            }
        });

    }

    protected int getStockOnHandLevel(StockCardViewModel stockCardViewModel) {

        int lowStockAvg = stockRepository.getLowStockAvg(stockCardViewModel.getStockCard());

        long stockOnHand = stockCardViewModel.getStockOnHand();

        if (stockOnHand > lowStockAvg) {
            return STOCK_ON_HAND_NORMAL;
        } else if (stockOnHand > 0) {
            return STOCK_ON_HAND_LOW_STOCK;
        } else {
            return STOCK_ON_HAND_STOCK_OUT;
        }
    }

    public interface OnItemViewClickListener {
        void onItemViewClick(StockCardViewModel stockCardViewModel);
    }
}
