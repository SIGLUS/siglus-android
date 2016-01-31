package org.openlmis.core.view.holder;

import android.text.Html;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import org.apache.commons.lang.StringUtils;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.widget.InputFilterMinMax;

public class UnpackKitViewHolder extends PhysicalInventoryViewHolder {

    private final ViewGroup vg_soh_pop;

    public UnpackKitViewHolder(View itemView) {
        super(itemView);
        vg_soh_pop = (ViewGroup) itemView.findViewById(R.id.vg_soh_pop);
        etQuantity.setHint(R.string.hint_quantity_in_unpack_kit);
    }

    public void populate(InventoryViewModel inventoryViewModel) {
        etQuantity.setFilters(new InputFilter[]{new InputFilterMinMax(0, 2 * (int) inventoryViewModel.getKitExpectQuantity() - 1)});

        populate(inventoryViewModel, StringUtils.EMPTY);

        tvStockOnHandInInventoryTip.setText(context.getString(R.string.label_unpack_kit_quantity_expected));
        tvStockOnHandInInventory.setText(Long.toString(inventoryViewModel.getKitExpectQuantity()));

        updatePop(inventoryViewModel, inventoryViewModel.getQuantity());
    }

    @Override
    public void afterQuantityChanged(InventoryViewModel viewModel, String quantity) {
        super.afterQuantityChanged(viewModel, quantity);
        updatePop(viewModel, quantity);
    }

    protected void updatePop(InventoryViewModel viewModel, String quantity) {
        if (!LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_warning_unpack_kit_quantity)) {
            return;
        }

        if (TextUtils.isEmpty(quantity)) {
            setDefaultPop();
            return;
        }

        long quantityLong = Long.parseLong(quantity);
        long kitExpectQuantity = viewModel.getKitExpectQuantity();

        if ((quantityLong > kitExpectQuantity)) {
            setWarningPopLabel(Html.fromHtml(context.getString(R.string.label_unpack_kit_quantity_more_than_expected)));
        } else if (quantityLong < kitExpectQuantity) {
            setWarningPopLabel(Html.fromHtml(context.getString(R.string.label_unpack_kit_quantity_less_than_expected)));
        } else {
            setDefaultPop();
        }
    }

    private void setWarningPopLabel(Spanned text) {
        tvStockOnHandInInventoryTip.setText(text);
        tvStockOnHandInInventoryTip.setTextColor(context.getResources().getColor(R.color.color_warning_text_unpack_kit_pop));
        tvStockOnHandInInventoryTip.setGravity(Gravity.LEFT);
        tvStockOnHandInInventory.setTextColor(context.getResources().getColor(R.color.color_warning_text_unpack_kit_pop));
        vg_soh_pop.setBackgroundResource(R.drawable.inventory_pop_warning);
    }

    private void setDefaultPop() {
        tvStockOnHandInInventoryTip.setTextColor(context.getResources().getColor(R.color.color_text_secondary));
        tvStockOnHandInInventoryTip.setText(context.getString(R.string.label_unpack_kit_quantity_expected));
        tvStockOnHandInInventoryTip.setGravity(Gravity.RIGHT);
        tvStockOnHandInInventory.setTextColor(context.getResources().getColor(R.color.color_text_secondary));
        vg_soh_pop.setBackgroundResource(R.drawable.inventory_pop);
    }
}
