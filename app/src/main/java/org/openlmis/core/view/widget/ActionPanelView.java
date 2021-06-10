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
import android.widget.Button;
import android.widget.FrameLayout;
import lombok.Getter;
import org.openlmis.core.R;
import roboguice.RoboGuice;
import roboguice.inject.InjectView;

@Getter
public class ActionPanelView extends FrameLayout {

  @InjectView(R.id.btn_save)
  View btnSave;

  @InjectView(R.id.btn_complete)
  Button btnComplete;

  public ActionPanelView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  protected void init(Context context) {
    inflate(context, R.layout.view_action_panel, this);
    RoboGuice.injectMembers(getContext(), this);
    RoboGuice.getInjector(getContext()).injectViewMembers(this);
  }

  public void setListener(OnClickListener positiveClickListener,
      OnClickListener negativeClickListener) {
    btnComplete.setOnClickListener(positiveClickListener);
    btnSave.setOnClickListener(negativeClickListener);
  }

  public void setPositiveButtonText(String buttonName) {
    btnComplete.setText(buttonName);
  }

  public void setNegativeButtonVisibility(int visibility) {
    btnSave.setVisibility(visibility);
  }
}
