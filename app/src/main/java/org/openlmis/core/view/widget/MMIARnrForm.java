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

    public MMIARnrForm(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        setOrientation(LinearLayout.VERTICAL);
    }

    public MMIARnrForm(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MMIARnrForm(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void initView(ArrayList<RnrFormItem> rnrFormItemList) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View container = layoutInflater.inflate(R.layout.view_mmia_rnr_form, this);
        leftViewGroup = (ViewGroup) container.findViewById(R.id.rnr_from_list_product_name);
        rightViewGroup = (ViewGroup) container.findViewById(R.id.rnr_from_list);
        addLeftHeaderView();
        addRightHeaderView();
        for (RnrFormItem item : rnrFormItemList) {
            View leftView = layoutInflater.inflate(R.layout.item_rnr_from_product_name, this, false);
            addLeftView(leftView, item);
            View spaceLine = layoutInflater.inflate(R.layout.view_space_line, this, false);
            leftViewGroup.addView(spaceLine);
            View view = layoutInflater.inflate(R.layout.item_rnr_from, this, false);
            addRightView(view, item);
            View spaceLine2 = layoutInflater.inflate(R.layout.view_space_line, this, false);
            rightViewGroup.addView(spaceLine2);
        }
    }


    private void addLeftHeaderView() {
        View view = LayoutInflater.from(context).inflate(R.layout.item_rnr_from_product_name, this, false);
        TextView tvPrimaryName = (TextView) view.findViewById(R.id.tv_primary_name);
        tvPrimaryName.setText(R.string.list_rnrfrom_left_header);
        tvPrimaryName.setGravity(Gravity.CENTER);
        view.setBackgroundResource(R.color.color_mmia_info_name);
        leftViewGroup.addView(view);
    }

    private void addLeftView(View view, RnrFormItem item) {
        TextView tvPrimaryName = (TextView) view.findViewById(R.id.tv_primary_name);
        Product product = item.getProduct();
        tvPrimaryName.setText(product.getPrimaryName());
        leftViewGroup.addView(view);
    }

    private void addRightHeaderView() {
        View inflate = View.inflate(context, R.layout.item_rnr_from, null);
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

    private void addRightView(View inflate, RnrFormItem item) {
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
    }


}
