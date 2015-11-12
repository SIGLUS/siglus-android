package org.openlmis.core.view.holder;

import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.repository.RnrFormItemRepository;
import org.openlmis.core.utils.ToastUtil;

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

    protected RnrFormItemRepository rnrFormItemRepository;
    private OnItemViewClickListener listener;

    protected static final int STOCK_ON_HAND_NORMAL = 1;
    protected static final int STOCK_ON_HAND_LOW_STOCK = 2;
    protected static final int STOCK_ON_HAND_STOCK_OUT = 3;

    public StockCardViewHolder(View itemView, OnItemViewClickListener listener) {
        super(itemView);
        this.listener = listener;
        this.rnrFormItemRepository = RoboGuice.getInjector(context).getInstance(RnrFormItemRepository.class);
    }

    public void populate(final StockCard stockCard) {

        final Product product = stockCard.getProduct();

        tvProductName.setText(getStyledProductName(product));
        tvProductUnit.setText(getStyledProductUnit(product));

        initStockOnHand(stockCard);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemViewClick(stockCard);
                }
            }
        });
    }

    @NonNull
    private SpannableStringBuilder getStyledProductUnit(Product product) {
        String unit = product.getStrength() + " " + product.getType();
        SpannableStringBuilder styledUnit = new SpannableStringBuilder(unit);
        int length = 0;
        if (product.getStrength() != null) {
            length = product.getStrength().length();
        }
        styledUnit.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.secondary_text)),
                length, unit.length(), Spannable.SPAN_POINT_MARK);
        return styledUnit;
    }

    @NonNull
    private SpannableStringBuilder getStyledProductName(Product product) {
        String productName = product.getFormattedProductName();
        SpannableStringBuilder styledName = new SpannableStringBuilder(productName);
        styledName.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.secondary_text)),
                product.getPrimaryName().length(), productName.length(), Spannable.SPAN_POINT_MARK);
        return styledName;
    }


    private void initStockOnHand(final StockCard stockCard) {
        tvStockOnHand.setText(stockCard.getStockOnHand() + "");

        int stockOnHandLevel = getStockOnHandLevel(stockCard);
        String warningMsg = null;

        switch (stockOnHandLevel) {
            case STOCK_ON_HAND_LOW_STOCK:
                stockOnHandBg.setBackgroundResource(R.color.color_low_stock);
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

    protected int getStockOnHandLevel(StockCard stockCard) {
        int lowStockAvg = rnrFormItemRepository.getLowStockAvg(stockCard.getProduct());
        long stockOnHand = stockCard.getStockOnHand();

        if (stockOnHand > lowStockAvg) {
            return STOCK_ON_HAND_NORMAL;
        } else if (stockOnHand > 0) {
            return STOCK_ON_HAND_LOW_STOCK;
        } else {
            return STOCK_ON_HAND_STOCK_OUT;
        }
    }

    public interface OnItemViewClickListener {
        void onItemViewClick(StockCard stockCard);
    }
}
