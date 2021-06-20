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

package roboguice.actiivty;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import org.openlmis.core.googleanalytics.ScreenName;
import org.openlmis.core.view.activity.BaseActivity;

public class FragmentControllerActivity  extends BaseActivity {

  public static final int ID_CONTAINER = View.generateViewId();

  @Override
  protected ScreenName getScreenName() {
    return null;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    LinearLayout view = new LinearLayout(this);
    view.setId(ID_CONTAINER);

    setContentView(view);
  }
}