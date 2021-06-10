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
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.openlmis.core.R;
import org.openlmis.core.view.viewmodel.ViaKitsViewModel;
import roboguice.RoboGuice;
import roboguice.inject.InjectView;

public class ViaKitView extends LinearLayout {

  @InjectView(R.id.et_via_kit_received_hf)
  TextView etKitReceivedHF;

  @InjectView(R.id.et_via_kit_received_chw)
  TextView etKitReceivedCHW;

  @InjectView(R.id.et_via_kit_opened_hf)
  TextView etKitOpenedHF;

  @InjectView(R.id.et_via_kit_opened_chw)
  TextView etKitOpenedCHW;

  @Getter
  boolean hasDataChanged;

  public ViaKitView(Context context) {
    super(context);
    init(context);
  }

  public ViaKitView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  private void init(Context context) {
    setOrientation(LinearLayout.VERTICAL);
    LayoutInflater.from(context).inflate(R.layout.view_via_kit, this);
    RoboGuice.injectMembers(getContext(), this);
    RoboGuice.getInjector(getContext()).injectViewMembers(this);
  }

  public void setValue(ViaKitsViewModel viaKitsViewModel) {
    etKitReceivedHF.setText(viaKitsViewModel.getKitsReceivedHF());
    etKitOpenedHF.setText(viaKitsViewModel.getKitsOpenedHF());
    etKitReceivedCHW.setText(viaKitsViewModel.getKitsReceivedCHW());
    etKitOpenedCHW.setText(viaKitsViewModel.getKitsOpenedCHW());
  }

  public void setEditClickListener(OnClickListener listener) {
    etKitReceivedHF.setFocusable(false);
    etKitOpenedHF.setFocusable(false);
    etKitReceivedCHW.setFocusable(false);
    etKitOpenedCHW.setFocusable(false);
    etKitReceivedHF.setOnClickListener(listener);
    etKitOpenedHF.setOnClickListener(listener);
    etKitReceivedCHW.setOnClickListener(listener);
    etKitOpenedCHW.setOnClickListener(listener);
  }

  public void setEmergencyKitValues() {
    etKitReceivedHF.setText(StringUtils.EMPTY);
    etKitOpenedHF.setText(StringUtils.EMPTY);
    etKitReceivedCHW.setText(StringUtils.EMPTY);
    etKitOpenedCHW.setText(StringUtils.EMPTY);
  }
}
