package org.openlmis.core.view.holder;

import android.view.View;
import android.widget.TextView;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.utils.TextStyleUtil;
import org.openlmis.core.view.viewmodel.StockCardViewModel;

import roboguice.inject.InjectView;

public class ArchivedDrugsViewHolder extends BaseViewHolder{

    @InjectView(R.id.product_name)
    TextView tvProductName;

    @InjectView(R.id.product_unit)
    TextView tvProductUnit;

    public ArchivedDrugsViewHolder(View itemView) {
        super(itemView);
    }

    public void populate(final StockCardViewModel stockCardViewModel, String queryKeyWord) {

        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.search_view_enhancement)) {
            tvProductName.setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, stockCardViewModel.getStyledName()));
            tvProductUnit.setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, stockCardViewModel.getStyledUnit()));
        } else {
            tvProductName.setText(stockCardViewModel.getStyledName());
            tvProductUnit.setText(stockCardViewModel.getStyledUnit());
        }
    }
}
