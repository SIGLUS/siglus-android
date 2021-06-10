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

import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.ArrayAdapter;
import java.util.ArrayList;
import org.openlmis.core.R;
import org.openlmis.core.manager.MovementReasonManager;
import org.roboguice.shaded.goole.common.collect.FluentIterable;


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
    contentList = newArrayList(
        context.getResources().getStringArray(R.array.movement_type_items_array));

    adapter = new ArrayAdapter<>(context, R.layout.item_movement_type, R.id.tv_option, contentList);

    builder.setAdapter(adapter, null);
    dialog = builder.create();

    dialog.getListView().setOnItemClickListener((parent, view, position, id) -> {
      if (status == STATUS_FIRST_MENU) {
        status = position;
        showSecondaryList();
      } else {
        performOnSelect(position);
        dialog.dismiss();
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

    MovementReasonManager.MovementType type = null;
    switch (status) {
      case STATUS_SELECT_RECEIVE:
        type = MovementReasonManager.MovementType.RECEIVE;
        break;
      case STATUS_SELECT_NEGATIVE:
        type = MovementReasonManager.MovementType.NEGATIVE_ADJUST;
        break;
      case STATUS_SELECT_POSITIVE:
        type = MovementReasonManager.MovementType.POSITIVE_ADJUST;
        break;
      case STATUS_SELECT_ISSUE:
        type = MovementReasonManager.MovementType.ISSUE;
        break;
      default:
        // do nothing
    }

    if (type != null) {
      movementReasons.addAll(reasonManager.buildReasonListForMovementType(type));

      contentList.addAll(FluentIterable.from(reasonManager.buildReasonListForMovementType(type))
          .transform(reason -> reason.getDescription()).toList());
    }

    adapter.notifyDataSetChanged();
  }

  public interface OnMovementSelectListener {

    void onComplete(MovementReasonManager.MovementReason reason);
  }

}
