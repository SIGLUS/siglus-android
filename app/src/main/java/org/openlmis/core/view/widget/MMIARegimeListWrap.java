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
import androidx.annotation.Nullable;
import java.util.List;
import org.openlmis.core.R;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.presenter.MMIARequisitionPresenter;

public class MMIARegimeListWrap extends LinearLayout {

  private LayoutInflater layoutInflater;
  private MMIARegimeList regimeList;
  private LinearLayout regimeLeftHeader;
  private TextView leftHeaderAdult;
  private TextView leftHeaderChildren;

  public MMIARegimeListWrap(Context context) {
    super(context);
    init(context);
  }

  public MMIARegimeListWrap(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  private void init(Context context) {
    layoutInflater = LayoutInflater.from(context);
    regimeList = (MMIARegimeList) layoutInflater
        .inflate(R.layout.fragment_mmia_requisition_regime_list_conent, this, false);
    regimeLeftHeader = (LinearLayout) layoutInflater
        .inflate(R.layout.fragment_mmia_requisition_regime_left_header, this, false);
    leftHeaderAdult = regimeLeftHeader.findViewById(R.id.regime_left_header_adult);
    leftHeaderChildren = regimeLeftHeader.findViewById(R.id.regime_left_header_children);
  }


  public void initView(TextView totalView, TextView totalPharmacy, TextView tvTotalPharmacyTitle,
      MMIARequisitionPresenter presenter) {
    regimeList.initView(totalView, totalPharmacy, tvTotalPharmacyTitle, presenter);
    addView(regimeLeftHeader);
    addView(regimeList);
    leftHeaderAdult.setBackgroundResource(R.color.color_green_light);
    leftHeaderChildren.setBackgroundResource(R.color.color_regime_baby);

    if (regimeList.isPharmacyEmpty) {
      regimeLeftHeader.setVisibility(GONE);
    }
    regimeList.post(this::updateLeftHeader);
  }

  public void updateLeftHeader() {
    LayoutParams adultParams = (LayoutParams) leftHeaderAdult.getLayoutParams();
    adultParams.height = regimeList.adultHeight;
    leftHeaderAdult.setLayoutParams(adultParams);
    LayoutParams childrenParams = (LayoutParams) leftHeaderChildren.getLayoutParams();
    childrenParams.height = regimeList.childrenHeight;
    leftHeaderChildren.setLayoutParams(childrenParams);
  }

  public List<RegimenItem> getDataList() {
    return regimeList.getDataList();
  }

  public boolean isCompleted() {
    return regimeList.isCompleted();
  }

  public void deHighLightTotal() {
    regimeList.deHighLightTotal();
  }

  public void addCustomRegimenItem(Regimen regimen) {
    regimeList.addCustomRegimenItem(regimen);
  }

  public void setRegimeListener(MMIARegimeList.MMIARegimeListener regimeListener) {
    regimeList.setRegimeListener(regimeListener);
  }
}
