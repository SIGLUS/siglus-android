package org.openlmis.core.view.holder;

import android.view.View;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.utils.TextStyleUtil;
import org.openlmis.core.view.viewmodel.StockCardViewModel;

import roboguice.inject.InjectView;

public class ArchivedDrugsViewHolder extends BaseViewHolder {

    @InjectView(R.id.product_name)
    TextView tvProductName;

    @InjectView(R.id.product_unit)
    TextView tvProductUnit;

    @InjectView(R.id.action_view_history)
    TextView tvViewHistory;

    @InjectView(R.id.action_archive_back)
    TextView tvArchiveBack;

    public ArchivedDrugsViewHolder(View itemView) {
        super(itemView);
    }

    public void populate(final StockCardViewModel stockCardViewModel, String queryKeyWord, final ArchiveStockCardListener listener) {

        tvProductName.setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, stockCardViewModel.getStyledName()));
        tvProductUnit.setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, stockCardViewModel.getStyledUnit()));

        setActionListeners(stockCardViewModel, listener);
    }

    private void setActionListeners(final StockCardViewModel stockCardViewModel, final ArchiveStockCardListener listener) {
        tvViewHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.viewMovementHistory(stockCardViewModel.getStockCard());
                }
            }
        });

        tvArchiveBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.archiveStockCardBack(stockCardViewModel.getStockCard());
                }
            }
        });
    }

    public interface ArchiveStockCardListener {
        void viewMovementHistory(StockCard stockCard);

        void archiveStockCardBack(StockCard stockCard);
    }
}
