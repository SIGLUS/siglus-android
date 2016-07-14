package org.openlmis.core.view.holder;

import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.R;
import org.openlmis.core.utils.TextStyleUtil;
import org.openlmis.core.view.viewmodel.InventoryViewModel;

import roboguice.inject.InjectView;


public class AddDrugsToFormViewHolder extends BaseViewHolder {

    @InjectView(R.id.product_name)
    TextView productName;

    @InjectView(R.id.tv_short_code)
    TextView tvShortCode;

    @InjectView(R.id.touchArea_checkbox)
    LinearLayout taCheckbox;

    @InjectView(R.id.checkbox)
    CheckBox checkBox;

    @InjectView(R.id.action_panel)
    View actionPanel;

    @InjectView(R.id.tx_quantity)
    EditText txQuantity;

    @InjectView(R.id.action_divider)
    View actionDivider;

    public AddDrugsToFormViewHolder(View itemView) {
        super(itemView);
        taCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                triggerCheckbox();
            }
        });
    }

    public void populate(String queryKeyWord, final InventoryViewModel viewModel) {
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == viewModel.isChecked()) {
                    return;
                }

                if (isChecked) {
                    showEditPanel(View.VISIBLE);
                } else {
                    showEditPanel(View.GONE);
                    populateEditPanel(StringUtils.EMPTY);

                    viewModel.setQuantity(StringUtils.EMPTY);
                }
                viewModel.setChecked(isChecked);
            }
        });
        checkBox.setChecked(viewModel.isChecked());

        productName.setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, viewModel.getStyledName()));
        tvShortCode.setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, viewModel.getStyledUnit()));
    }

    protected void populateEditPanel(String quantity) {
        txQuantity.setText(quantity);
    }

    protected void showEditPanel(int visible) {
        actionDivider.setVisibility(visible);
        actionPanel.setVisibility(visible);
    }

    private void triggerCheckbox() {
        if (checkBox.isChecked()) {
            checkBox.setChecked(false);
        } else {
            checkBox.setChecked(true);
        }
    }
}
