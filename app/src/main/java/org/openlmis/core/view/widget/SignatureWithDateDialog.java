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

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import org.openlmis.core.R;
import roboguice.inject.InjectView;

public class SignatureWithDateDialog extends SignatureDialog {

  @InjectView(R.id.et_process_date)
  private TextView etProcessDate;

  boolean isHideTitle = false;

  @Override
  protected int getSignatureLayoutId() {
    return R.layout.dialog_signature_with_date;
  }

  @Override
  public void onStart() {
    super.onStart();
    initPrecessDate();
    if (isHideTitle) {
      tvSignatureTitle.setVisibility(View.GONE);
    }
  }

  public void hideTitle() {
    isHideTitle = true;
  }

  public static Bundle getBundleToMe(String date) {
    Bundle bundle = new Bundle();
    bundle.putString("Date", date);
    return bundle;
  }

  private void initPrecessDate() {
    Bundle arguments = getArguments();
    if (arguments != null) {
      etProcessDate.setText(arguments.getString("Date"));
    }
  }
}
