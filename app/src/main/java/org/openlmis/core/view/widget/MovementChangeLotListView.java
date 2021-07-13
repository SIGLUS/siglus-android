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
import org.openlmis.core.view.adapter.LotMovementAdapter;
import org.openlmis.core.view.viewmodel.BaseStockMovementViewModel;

public class MovementChangeLotListView extends BaseLotListView {

  LotMovementAdapter.MovementChangedListener movementChangedListener;

  public void initLotListView(BaseStockMovementViewModel viewModel,
      LotMovementAdapter.MovementChangedListener listener) {
    movementChangedListener = listener;
    super.initLotListView(viewModel);
  }

  public MovementChangeLotListView(Context context) {
    super(context);
  }

  public MovementChangeLotListView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public void initNewLotListView() {
    super.initNewLotListView();
    newLotMovementAdapter.setMovementChangeListener(movementChangedListener);
  }

  @Override
  public void initExistingLotListView() {
    super.initExistingLotListView();
    existingLotMovementAdapter.setMovementChangeListener(movementChangedListener);
  }
}
