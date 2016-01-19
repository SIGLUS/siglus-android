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

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import org.openlmis.core.R;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.StockMovementItem;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.base.Predicate;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.util.ArrayList;

import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;


public class MovementTypeDialog {

    static final int STATUS_FIRST_MENU = -1;
    static final int STATUS_SELECT_RECEIVE = 0;
    static final int STATUS_SELECT_NEGATIVE = 1;
    static final int STATUS_SELECT_POSITIVE = 2;
    static final int STATUS_SELECT_ISSUE = 3;
    AlertDialog dialog;
    ArrayList<String> contentList;
    ArrayList<MovementReasonManager.MovementReason> movementReasons;


    ArrayAdapter<String> adapter;
    AlertDialog.Builder builder;
    Context context;
    OnMovementSelectListener listener;
    int status = STATUS_FIRST_MENU;

    protected MovementReasonManager reasonManager;


    public MovementTypeDialog(Context context, final OnMovementSelectListener listener) {

        this.context = context;
        this.listener = listener;

        builder = new AlertDialog.Builder(context);
        contentList = newArrayList(context.getResources().getStringArray(R.array.movement_type_items_array));

        adapter = new ArrayAdapter<>(context, R.layout.item_movement_type, R.id.text, contentList);

        builder.setAdapter(adapter, null);
        dialog = builder.create();

        dialog.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (status == STATUS_FIRST_MENU) {
                    status = position;
                    showSecondaryList();
                } else {
                    performOnSelect(position);
                    dialog.dismiss();
                }
            }
        });

        reasonManager = MovementReasonManager.getInstance();
        movementReasons = new ArrayList<>();
    }

    private void performOnSelect(int position) {
        if (listener != null) {
            listener.onComplete(movementReasons.get(position));
        }
    }


    public void show() {
        dialog.show();
    }

    private void showSecondaryList() {
        contentList.clear();

        StockMovementItem.MovementType type = null;
        switch (status) {
            case STATUS_SELECT_RECEIVE:
                type = StockMovementItem.MovementType.RECEIVE;
                break;
            case STATUS_SELECT_NEGATIVE:
                type = StockMovementItem.MovementType.NEGATIVE_ADJUST;
                break;
            case STATUS_SELECT_POSITIVE:
                type = StockMovementItem.MovementType.POSITIVE_ADJUST;
                break;
            case STATUS_SELECT_ISSUE:
                type = StockMovementItem.MovementType.ISSUE;
                break;
        }

        if (type !=null){
            movementReasons.addAll(reasonManager.buildReasonListForMovementType(type));

            contentList.addAll(FluentIterable.from(reasonManager.buildReasonListForMovementType(type)).filter(new Predicate<MovementReasonManager.MovementReason>() {
                @Override
                public boolean apply(MovementReasonManager.MovementReason movementReason) {
                    return !MovementReasonManager.UNPACK_KIT.equals(movementReason.getCode());
                }
            }).transform(new Function<MovementReasonManager.MovementReason, String>() {
                @Override
                public String apply(MovementReasonManager.MovementReason reason) {
                    return reason.getDescription();
                }
            }).toList());
        }

        adapter.notifyDataSetChanged();
    }

    public  interface OnMovementSelectListener {
        void onComplete(MovementReasonManager.MovementReason reason);
    }

}
