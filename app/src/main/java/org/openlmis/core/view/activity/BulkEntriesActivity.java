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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;
import java.io.Serializable;
import org.openlmis.core.R;
import org.openlmis.core.googleanalytics.ScreenName;
import roboguice.inject.ContentView;

@ContentView(R.layout.activity_bulk_entries)
public class BulkEntriesActivity extends BaseActivity {

  @Override
  protected ScreenName getScreenName() {
    return ScreenName.BULK_ENTRIES_SCREEN;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    getMenuInflater().inflate(R.menu.menu_bulk_entries, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.action_add_product) {
      Intent intent = new Intent(this, AddProductsToBulkEntriesActivity.class);
      startActivity(intent);
      // openAddProductSActivityForResult();
      return true;
    } else {
      return super.onOptionsItemSelected(item);
    }
  }

//  public void openAddProductSActivityForResult() {
//    Intent intent = new Intent(getApplicationContext(),AddProductsToBulkEntriesActivity.class);
//    intent.putExtra(AddProductsToBulkEntriesActivity.SELECTED_PRODUCTS
//        ,(Serializable)bulkEntriesPresenter.getAllAddedProducts());
//    addProductsActivityResultLauncher.launch(intent);
//  }

  ActivityResultLauncher<Intent> addProductsActivityResultLauncher = registerForActivityResult(
      new StartActivityForResult(),
      result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
          Intent data = result.getData();

        }
      });
}
