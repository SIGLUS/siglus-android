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

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import java.util.Date;
import java.util.List;
import org.openlmis.core.R;
import org.openlmis.core.googleanalytics.ScreenName;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.view.fragment.VIARequisitionFragment;
import roboguice.inject.ContentView;


@ContentView(R.layout.activity_via_requisition)
public class VIARequisitionActivity extends BaseActivity {

  @Override
  protected ScreenName getScreenName() {
    return ScreenName.VIA_REQUISITION_SCREEN;
  }

  @Override
  protected int getThemeRes() {
    return R.style.AppTheme_PURPLE;
  }

  @Override
  public void onBackPressed() {
    ((VIARequisitionFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_requisition))
        .onBackPressed();
  }

  //For existing requisition
  public static Intent getIntentToMe(Context context, long formId) {
    Intent intent = new Intent(context, VIARequisitionActivity.class);
    intent.putExtra(Constants.PARAM_FORM_ID, formId);
    return intent;
  }

  //For creating new requisition
  public static Intent getIntentToMe(Context context, Date inventory, boolean isMissedPeriod) {
    Intent intent = new Intent(context, VIARequisitionActivity.class);
    intent.putExtra(Constants.PARAM_SELECTED_INVENTORY_DATE, inventory);
    intent.putExtra(Constants.PARAM_IS_MISSED_PERIOD, isMissedPeriod);
    return intent;
  }

  //For emergency requisition
  public static Intent getIntentToMe(Context context, List<StockCard> stockCards) {
    Intent intent = new Intent(context, VIARequisitionActivity.class);
    intent.putExtra(Constants.PARAM_SELECTED_EMERGENCY, (Parcelable) stockCards);
    return intent;
  }

  public static Intent getIntentToMe(Context context) {
    return new Intent(context, VIARequisitionActivity.class);
  }
}
