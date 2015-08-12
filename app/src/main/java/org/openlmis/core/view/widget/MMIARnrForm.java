/*
 * This program is part of the OpenLMIS logistics management information
 * system platform software.
 *
 * Copyright © 2015 ThoughtWorks, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should
 * have received a copy of the GNU Affero General Public License along with
 * this program. If not, see http://www.gnu.org/licenses. For additional
 * information contact info@OpenLMIS.org
 */
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MMIARnrForm extends LinearLayout {
    private ViewGroup leftViewGroup;
    private ViewGroup rightViewGroup;
    private LayoutInflater layoutInflater;

    public MMIARnrForm(Context context) {
        super(context);
        init(context);
    }

    public MMIARnrForm(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setOrientation(LinearLayout.VERTICAL);
        layoutInflater = LayoutInflater.from(context);
        View container = layoutInflater.inflate(R.layout.view_mmia_rnr_form, this);
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

    private void addRightView(RnrFormItem item) {
        addRightView(item, false);
    }

    private void addLeftView(RnrFormItem item) {
        addLeftView(item, false);
    }

    private View inflaterDividerLine(LayoutInflater layoutInflater) {
        return layoutInflater.inflate(R.layout.view_space_line, this, false);
    }


    private View addLeftHeaderView() {
        return addLeftView(null, true);
    }

    private View inflaterLeftView() {
        return layoutInflater.inflate(R.layout.item_rnr_from_product_name, this, false);
    }

    private View addLeftView(RnrFormItem item, boolean isHeaderView) {
        View view = inflaterLeftView();
        TextView tvPrimaryName = (TextView) view.findViewById(R.id.tv_primary_name);
        if (isHeaderView) {
            tvPrimaryName.setText(R.string.list_rnrfrom_left_header);
            tvPrimaryName.setGravity(Gravity.CENTER);
            view.setBackgroundResource(R.color.color_mmia_info_name);
        } else {
            Product product = item.getProduct();
            tvPrimaryName.setText(product.getPrimaryName());
        }
        leftViewGroup.addView(view);
        leftViewGroup.addView(inflaterDividerLine(layoutInflater));
        return view;
    }

    private void addRightHeaderView() {
        addRightView(null, true);
    }

    private void addRightView(RnrFormItem item, boolean isHeaderView) {
        View inflate = layoutInflater.inflate(R.layout.item_rnr_from, this, false);
        TextView tvIssuedUnit = (TextView) inflate.findViewById(R.id.tv_issued_unit);
        TextView tvInitialAmount = (TextView) inflate.findViewById(R.id.tv_initial_amount);
        TextView tvReceived = (TextView) inflate.findViewById(R.id.tv_received);
        TextView tvIssued = (TextView) inflate.findViewById(R.id.tv_issued);
        TextView tvAdjustment = (TextView) inflate.findViewById(R.id.tv_adjustment);
        TextView tvInventory = (TextView) inflate.findViewById(R.id.tv_inventory);
        TextView tvValidate = (TextView) inflate.findViewById(R.id.tv_validate);

        if (isHeaderView) {
            tvIssuedUnit.setText(R.string.issued_unit);
            tvInitialAmount.setText(R.string.initial_amount);
            tvReceived.setText(R.string.received);
            tvIssued.setText(R.string.issued);
            tvAdjustment.setText(R.string.adjustment);
            tvInventory.setText(R.string.inventory);
            tvValidate.setText(R.string.validate);
            inflate.setBackgroundResource(R.color.color_mmia_info_name);
        } else {
            //TODO refactor api field tvIssuedUnit
            tvIssuedUnit.setText(String.valueOf(item.getProduct().getStrength()));
            tvInitialAmount.setText(String.valueOf(item.getInitialAmount()));
            tvReceived.setText(String.valueOf(item.getReceived()));
            tvIssued.setText(String.valueOf(item.getIssued()));
            tvAdjustment.setText(String.valueOf(item.getAdjustment()));
            tvInventory.setText(String.valueOf(item.getInventory()));

            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            try {
                Date parse = sdf.parse(item.getValidate());
                sdf = new SimpleDateFormat("MMM - yy");
                String formatDate = sdf.format(parse);
                tvValidate.setText(formatDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
        rightViewGroup.addView(inflate);
        rightViewGroup.addView(inflaterDividerLine(layoutInflater));
    }


}
