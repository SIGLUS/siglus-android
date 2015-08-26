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
    ArrayAdapter<String> adapter;
    AlertDialog.Builder builder;
    Context context;
    OnMovementSelectListener listener;
    int status = STATUS_FIRST_MENU;


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
    }

    private void performOnSelect(int position) {
        if (listener == null) {
            return;
        }

        listener.onComplete(contentList.get(position));

        switch (status) {
            case STATUS_SELECT_RECEIVE:
                listener.onReceive();
                break;
            case STATUS_SELECT_NEGATIVE:
                listener.onNegativeAdjustment();
                break;
            case STATUS_SELECT_POSITIVE:
                listener.onPositiveAdjustment();
                break;
            case STATUS_SELECT_ISSUE:
                listener.onIssue();
        }
    }


    public void show() {
        dialog.show();
    }

    private void showSecondaryList() {
        contentList.clear();
        int resId = 0;
        switch (status) {
            case STATUS_SELECT_RECEIVE:
                resId = R.array.movement_receive_items_array;
                break;
            case STATUS_SELECT_NEGATIVE:
                resId = R.array.movement_negative_items_array;
                break;
            case STATUS_SELECT_POSITIVE:
                resId = R.array.movement_positive_items_array;
                break;
            case STATUS_SELECT_ISSUE:
                resId = R.array.movement_issue_items_array;
                break;
        }
        contentList.addAll(newArrayList(context.getResources().getStringArray(resId)));
        adapter.notifyDataSetChanged();
    }

    public  interface OnMovementSelectListener {
        void onReceive();

        void onIssue();

        void onPositiveAdjustment();

        void onNegativeAdjustment();

        void onComplete(String result);
    }

}
