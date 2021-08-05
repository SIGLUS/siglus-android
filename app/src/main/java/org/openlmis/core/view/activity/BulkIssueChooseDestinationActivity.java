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

package org.openlmis.core.view.activity;

import static org.openlmis.core.view.activity.AddProductsToBulkEntriesActivity.IS_FROM_BULK_ISSUE;
import static org.openlmis.core.view.activity.AddProductsToBulkEntriesActivity.SELECTED_PRODUCTS;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;
import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputLayout;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import org.openlmis.core.R;
import org.openlmis.core.googleanalytics.ScreenName;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.manager.MovementReasonManager.MovementReason;
import org.openlmis.core.manager.MovementReasonManager.MovementType;
import org.openlmis.core.view.fragment.SimpleSelectDialogFragment;
import org.openlmis.core.view.widget.SingleClickButtonListener;
import org.roboguice.shaded.goole.common.collect.FluentIterable;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_bulk_issue_choose_destination)
public class BulkIssueChooseDestinationActivity extends BaseActivity {

  @InjectView(R.id.til_destination)
  TextInputLayout tilDestination;

  @InjectView(R.id.et_destination)
  EditText etDestination;

  @InjectView(R.id.bt_next)
  Button btNext;

  MovementReason chosenReason = null;

  private final ActivityResultLauncher<Intent> addProductsActivityResultLauncher = registerForActivityResult(
      new StartActivityForResult(), result -> {
        if (result.getResultCode() != Activity.RESULT_OK) {
          return;
        }
        Intent intent = new Intent(BulkIssueChooseDestinationActivity.this, BulkIssueActivity.class);
        intent.putExtra(SELECTED_PRODUCTS, result.getData().getSerializableExtra(SELECTED_PRODUCTS));
        startActivity(intent);
        finish();
      });

  @Override
  protected ScreenName getScreenName() {
    return ScreenName.BULK_ISSUE_CHOOSE_DESTINATION;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    btNext.setOnClickListener(getNextClickListener());
    etDestination.setOnClickListener(getMovementReasonOnClickListener());
  }

  private void updateMovementReason() {
    etDestination.setText(chosenReason == null ? "" : chosenReason.getDescription());
  }

  private boolean validate() {
    if (chosenReason == null) {
      tilDestination.setError(getString(R.string.msg_empty_movement_reason));
      return false;
    }
    return true;
  }

  @NonNull
  private SingleClickButtonListener getNextClickListener() {
    return new SingleClickButtonListener() {
      @Override
      public void onSingleClick(View v) {
        if (!validate()) {
          return;
        }
        Intent intent = new Intent(getApplicationContext(), AddProductsToBulkEntriesActivity.class);
        intent.putExtra(SELECTED_PRODUCTS, (Serializable) Collections.singletonList(""));
        intent.putExtra(IS_FROM_BULK_ISSUE, true);
        addProductsActivityResultLauncher.launch(intent);
      }
    };
  }

  @NonNull
  private View.OnClickListener getMovementReasonOnClickListener() {
    return new SingleClickButtonListener() {
      @Override
      public void onSingleClick(View view) {
        List<MovementReason> movementReasons = MovementReasonManager.getInstance()
            .buildReasonListForMovementType(MovementType.ISSUE);
        String[] reasonArray = FluentIterable.from(movementReasons).transform(MovementReason::getDescription)
            .toArray(String.class);
        Bundle bundle = new Bundle();
        bundle.putStringArray(SimpleSelectDialogFragment.SELECTIONS, reasonArray);
        SimpleSelectDialogFragment reasonsDialog = new SimpleSelectDialogFragment();
        reasonsDialog.setArguments(bundle);
        reasonsDialog.setMovementTypeOnClickListener((parent, view1, position, id) -> {
          tilDestination.setError(null);
          chosenReason = movementReasons.get(position);
          updateMovementReason();
          reasonsDialog.dismiss();
        });
        reasonsDialog.show(getSupportFragmentManager(), "SELECT_REASONS");
      }
    };
  }
}