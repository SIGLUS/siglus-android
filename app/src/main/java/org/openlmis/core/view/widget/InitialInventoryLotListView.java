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
import android.view.View;
import androidx.annotation.NonNull;
import lombok.Setter;
import org.openlmis.core.R;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;

public class InitialInventoryLotListView extends BaseLotListView {

  @Setter
  private UpdateCheckBoxListener updateCheckBoxListener;

  public InitialInventoryLotListView(Context context) {
    super(context);
  }

  public InitialInventoryLotListView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @NonNull
  @Override
  protected SingleClickButtonListener getAddNewLotDialogOnClickListener() {
    return new SingleClickButtonListener() {
      @Override
      public void onSingleClick(View v) {
        switch (v.getId()) {
          case R.id.btn_complete:
            if (addLotDialogFragment.validate()
                && addLotDialogFragment.isAdded()
                && !addLotDialogFragment.hasIdenticalLot(getLotNumbers())) {
              addNewLot(new LotMovementViewModel(addLotDialogFragment.getLotNumber(),
                  addLotDialogFragment.getExpiryDate(),
                  viewModel.getMovementType()));
              addLotDialogFragment.dismiss();
            }
            break;
          case R.id.btn_cancel:
            addLotDialogFragment.dismiss();
            break;
          default:
            // do nothing
        }
      }
    };
  }

  @NonNull
  @Override
  public OnDismissListener getOnAddNewLotDialogDismissListener() {
    return () -> {
      setActionAddNewEnabled(true);
      updateCheckBoxListener.updateCheckBox();
    };
  }

  public interface UpdateCheckBoxListener {

    void updateCheckBox();
  }
}
