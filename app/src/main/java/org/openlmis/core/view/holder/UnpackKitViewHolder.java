package org.openlmis.core.view.holder;

import android.text.Html;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;

import org.apache.commons.lang.StringUtils;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.widget.InputFilterMinMax;

public class UnpackKitViewHolder extends PhysicalInventoryViewHolder {

    public UnpackKitViewHolder(View itemView) {
        super(itemView);
        etQuantity.setHint(R.string.hint_quantity_in_unpack_kit);
    }

    public void populate(InventoryViewModel inventoryViewModel) {
        etQuantity.setFilters(new InputFilter[]{new InputFilterMinMax(0, 2 * (int) inventoryViewModel.getKitExpectQuantity() - 1)});

        populate(inventoryViewModel, StringUtils.EMPTY);

        tvStockOnHandInInventoryTip.setText(context.getString(R.string.label_unpack_kit_quantity_expected));
        tvStockOnHandInInventory.setText(Long.toString(inventoryViewModel.getKitExpectQuantity()));

    }

    //TODO change background

    @Override
    public void afterQuantityChanged(InventoryViewModel viewModel, String quantity) {
        super.afterQuantityChanged(viewModel, quantity);

        if (!LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_warning_unpack_kit_quantity)) {
            return;
        }

        if (TextUtils.isEmpty(quantity)) {
            setDefaultPop();
            return;
        }

        long quantityLong = Long.parseLong(quantity);
        long stockOnHand = viewModel.getKitExpectQuantity();

        if ((quantityLong > stockOnHand)) {
            setPopLabel(Html.fromHtml(context.getString(R.string.label_unpack_kit_quantity_more_than_expected)));
        } else if (quantityLong < stockOnHand) {
            setPopLabel(Html.fromHtml(context.getString(R.string.label_unpack_kit_quantity_less_than_expected)));
        } else {
            setDefaultPop();
        }
    }

    private void setPopLabel(Spanned text) {
        tvStockOnHandInInventoryTip.setText(text);
        tvStockOnHandInInventoryTip.setTextColor(context.getResources().getColor(R.color.color_warning_text_unpack_kit_pop));
        tvStockOnHandInInventoryTip.setGravity(Gravity.LEFT);
        tvStockOnHandInInventory.setTextColor(context.getResources().getColor(R.color.color_warning_text_unpack_kit_pop));
    }

    private void setDefaultPop() {
        tvStockOnHandInInventoryTip.setTextColor(context.getResources().getColor(R.color.color_text_secondary));
        tvStockOnHandInInventoryTip.setText(context.getString(R.string.label_unpack_kit_quantity_expected));
        tvStockOnHandInInventoryTip.setGravity(Gravity.RIGHT);
        tvStockOnHandInInventory.setTextColor(context.getResources().getColor(R.color.color_text_secondary));
    }
}
