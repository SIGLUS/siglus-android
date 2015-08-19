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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnrFormItem;


public class RequisitionForm extends LinearLayout{

    Context context;
    LayoutInflater layoutInflater;

    LinearLayout productNameList;
    LinearLayout requisitionBody;

    LinearLayout requisitionBodyHeader;
    LinearLayout productNameListHeader;

    RnRForm rnRForm;


    public RequisitionForm(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        initUI();
    }

    private void initUI() {
        productNameList = (LinearLayout)layoutInflater.inflate(R.layout.layout_requisition_body_left, this).findViewById(R.id.requisition_body_left);
        requisitionBody = (LinearLayout)layoutInflater.inflate(R.layout.layout_requisition_body_right, this).findViewById(R.id.requisition_body_right);

        requisitionBodyHeader = (LinearLayout)requisitionBody.findViewById(R.id.requisition_body_header);
        productNameListHeader = (LinearLayout)productNameList.findViewById(R.id.product_name_list_header);

        post(new Runnable() {
            @Override
            public void run() {
                productNameListHeader.getLayoutParams().height = requisitionBodyHeader.getHeight();
            }
        });
    }

    public void setData(RnRForm rnRForm){
        this.rnRForm = rnRForm;

        RnrFormItem item = rnRForm.getRnrFormItemList().iterator().next();
        for (int i=0;i<10;i++){
            final View rightView = addChildToRight(item);
            final View leftView = addChildToLeft(item);

            leftView.post(new Runnable() {
                @Override
                public void run() {
                    int leftHeight = leftView.getHeight();
                    int rightHeight = rightView.getHeight();
                    if (leftHeight > rightHeight) {
                        rightView.getLayoutParams().height = leftHeight;
                    } else {
                        leftView.getLayoutParams().height = rightHeight;
                    }
                }
            });
        }
    }


    private View addChildToRight(RnrFormItem product){
        View item = layoutInflater.inflate(R.layout.item_requisition_body_right, this,false);
        long received = product.getReceived();
        long total = product.getInitialAmount() + received - product.getIssued();
        long inventory = product.getInventory();

        ((TextView) item.findViewById(R.id.tx_initial_amount)).setText(String.valueOf(product.getInitialAmount()));
        ((TextView)item.findViewById(R.id.tx_received)).setText(String.valueOf(received));
        ((TextView)item.findViewById(R.id.tx_issued)).setText(String.valueOf(product.getIssued()));
        ((TextView)item.findViewById(R.id.tx_theoretical)).setText(String.valueOf(total));
        ((TextView)item.findViewById(R.id.tx_total)).setText("-");
        ((TextView)item.findViewById(R.id.tx_initial_amount)).setText(String.valueOf(inventory));
        ((TextView)item.findViewById(R.id.tx_different)).setText(String.valueOf(product.getAdjustment()-total));
        ((TextView)item.findViewById(R.id.tx_total_request)).setText(String.valueOf(received*2-inventory));

        requisitionBody.addView(item);
        return item;
    }

    private View addChildToLeft(RnrFormItem product) {
        View item = layoutInflater.inflate(R.layout.item_requisition_body_left, this, false);
        ((TextView)item.findViewById(R.id.tx_FNM)).setText(product.getProduct().getCode());
        ((TextView)item.findViewById(R.id.tx_product_name)).setText(product.getProduct().getPrimaryName());
        productNameList.addView(item);
        return item;
    }

}

