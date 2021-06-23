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
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.List;
import lombok.Setter;
import org.apache.commons.lang3.ArrayUtils;
import org.openlmis.core.R;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.manager.MovementReasonManager.MovementReason;
import org.openlmis.core.manager.MovementReasonManager.MovementType;
import org.openlmis.core.presenter.BulkEntriesPresenter;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.view.activity.BulkEntriesActivity;
import org.openlmis.core.view.adapter.BulkEntriesLotMovementAdapter;
import org.openlmis.core.view.fragment.SimpleSelectDialogFragment;
import org.roboguice.shaded.goole.common.collect.FluentIterable;
import roboguice.inject.InjectView;


public class BulkEntriesLotListView extends BaseLotListView {

  private BulkEntriesLotMovementAdapter existingBulkEntriesLotMovementAdapter;


  public BulkEntriesLotListView(Context context) {
    super(context);
  }

  public BulkEntriesLotListView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  protected void init(Context context) {
    super.init(context);
    btnAddNewLot.setVisibility(GONE);
  }

  @Override
  public void initExistingLotListView() {
    super.initExistingLotListView();
    existingLotListView.setLayoutManager(new LinearLayoutManager(getContext()));
    existingBulkEntriesLotMovementAdapter = new BulkEntriesLotMovementAdapter(
        viewModel.getExistingLotMovementViewModelList(),getMovementReasonDescriptionList());
    existingLotListView.setAdapter(existingBulkEntriesLotMovementAdapter);
  }

  @Override
  public void initNewLotListView() {
    super.initNewLotListView();
  }

  public String[] getMovementReasonDescriptionList() {
    String[] reasonDescriptionList;
    List<MovementReason> movementReasons = MovementReasonManager
        .getInstance().buildReasonListForMovementType(MovementType.RECEIVE);
      reasonDescriptionList = FluentIterable.from(movementReasons)
          .transform(movementReason -> movementReason.getDescription()).toArray(String.class);
    return reasonDescriptionList;
  }

}
