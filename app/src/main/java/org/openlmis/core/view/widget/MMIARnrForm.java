package org.openlmis.core.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.RnrFormItem;

import java.util.ArrayList;

public class MMIARnrForm extends LinearLayout {
    private Context context;
    private ViewGroup leftViewGroup;
    private ViewGroup rightViewGroup;
    private LayoutInflater layoutInflater;
    private View container;

    public MMIARnrForm(Context context) {
        super(context);
        init(context);
    }

    public MMIARnrForm(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MMIARnrForm(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        setOrientation(LinearLayout.VERTICAL);
        layoutInflater = LayoutInflater.from(context);
        container = layoutInflater.inflate(R.layout.view_mmia_rnr_form, this);
        leftViewGroup = (ViewGroup) container.findViewById(R.id.rnr_from_list_product_name);
        rightViewGroup = (ViewGroup) container.findViewById(R.id.rnr_from_list);
    }

    public void initView(ArrayList<RnrFormItem> rnrFormItemList) {
        addLeftHeaderView();
        addRightHeaderView();
        for (RnrFormItem item : rnrFormItemList) {
            addLeftView(item);
            addRightView(item);
        }
    }

    private View inflaterDividerLine(LayoutInflater layoutInflater) {
        return layoutInflater.inflate(R.layout.view_space_line, this, false);
    }


    private void addLeftHeaderView() {
        View view = inflaterLeftView();
        TextView tvPrimaryName = (TextView) view.findViewById(R.id.tv_primary_name);
        tvPrimaryName.setText(R.string.list_rnrfrom_left_header);
        tvPrimaryName.setGravity(Gravity.CENTER);
        view.setBackgroundResource(R.color.color_mmia_info_name);
        leftViewGroup.addView(view);
        leftViewGroup.addView(inflaterDividerLine(layoutInflater));
    }

    private View inflaterLeftView() {
        return layoutInflater.inflate(R.layout.item_rnr_from_product_name, this, false);
    }

    private void addLeftView(RnrFormItem item) {
        View view = inflaterLeftView();
        TextView tvPrimaryName = (TextView) view.findViewById(R.id.tv_primary_name);
        Product product = item.getProduct();
        tvPrimaryName.setText(product.getPrimaryName());
        leftViewGroup.addView(view);
    }

    private void addRightHeaderView() {
        View inflate = layoutInflater.inflate(R.layout.item_rnr_from, this, false);
        TextView tvIssuedUnit = (TextView) inflate.findViewById(R.id.tv_issued_unit);
        TextView tvInitialAmount = (TextView) inflate.findViewById(R.id.tv_initial_amount);
        TextView tvReceived = (TextView) inflate.findViewById(R.id.tv_received);
        TextView tvIssued = (TextView) inflate.findViewById(R.id.tv_issued);
        TextView tvAdjustment = (TextView) inflate.findViewById(R.id.tv_adjustment);
        TextView tvInventory = (TextView) inflate.findViewById(R.id.tv_inventory);
        TextView tvValidate = (TextView) inflate.findViewById(R.id.tv_validate);

        tvIssuedUnit.setText(R.string.issued_unit);
        tvInitialAmount.setText(R.string.initial_amount);
        tvReceived.setText(R.string.received);
        tvIssued.setText(R.string.issued);
        tvAdjustment.setText(R.string.adjustment);
        tvInventory.setText(R.string.inventory);
        tvValidate.setText(R.string.validate);
        inflate.setBackgroundResource(R.color.color_mmia_info_name);
        rightViewGroup.addView(inflate);
    }

    private void addRightView(RnrFormItem item) {
        View inflate = layoutInflater.inflate(R.layout.item_rnr_from, this, false);
        TextView tvIssuedUnit = (TextView) inflate.findViewById(R.id.tv_issued_unit);
        TextView tvInitialAmount = (TextView) inflate.findViewById(R.id.tv_initial_amount);
        TextView tvReceived = (TextView) inflate.findViewById(R.id.tv_received);
        TextView tvIssued = (TextView) inflate.findViewById(R.id.tv_issued);
        TextView tvAdjustment = (TextView) inflate.findViewById(R.id.tv_adjustment);
        TextView tvInventory = (TextView) inflate.findViewById(R.id.tv_inventory);
        TextView tvValidate = (TextView) inflate.findViewById(R.id.tv_validate);

        //TODO refactor api field tvIssuedUnit
        tvIssuedUnit.setText(String.valueOf(item.getProduct().getStrength()));
        tvInitialAmount.setText(String.valueOf(item.getInitialAmount()));
        tvReceived.setText(String.valueOf(item.getReceived()));
        tvIssued.setText(String.valueOf(item.getIssued()));
        tvAdjustment.setText(String.valueOf(item.getAdjustment()));
        tvInventory.setText(String.valueOf(item.getInventory()));
        tvValidate.setText(String.valueOf(item.getValidate()));
        rightViewGroup.addView(inflate);
        rightViewGroup.addView(inflaterDividerLine(layoutInflater));
    }


}
