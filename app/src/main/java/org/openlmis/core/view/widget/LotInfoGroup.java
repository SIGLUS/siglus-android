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
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import java.util.Date;
import java.util.List;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.utils.DateUtil;
import org.roboguice.shaded.goole.common.collect.FluentIterable;
import org.roboguice.shaded.goole.common.collect.ImmutableList;
import roboguice.RoboGuice;

public class LotInfoGroup extends org.apmem.tools.layouts.FlowLayout {

  private LayoutInflater inflater;

  public LotInfoGroup(Context context, AttributeSet attributeSet) {
    super(context, attributeSet);
    init(context);
  }

  public void initLotInfoGroup(List<LotOnHand> lotOnHandList) {
    removeAllViews();
    ImmutableList<LotOnHand> sortedLotOnHandList = FluentIterable
        .from(lotOnHandList)
        .toSortedList((lotOnHand1, lotOnHand2) -> lotOnHand1.getLot().getExpirationDate()
            .compareTo(lotOnHand2.getLot().getExpirationDate()));
    for (LotOnHand lotOnHand : sortedLotOnHandList) {
      addLotInfoView(lotOnHand);
    }
  }

  private void init(Context context) {
    inflater = LayoutInflater.from(context);
    RoboGuice.getInjector(LMISApp.getContext()).injectMembersWithoutViews(this);
  }

  private void addLotInfoView(LotOnHand lotOnHand) {
    String lotOnHandQuantity = "" + lotOnHand.getQuantityOnHand();
    final Date expirationDate = lotOnHand.getLot().getExpirationDate();
    String lotInfo = lotOnHand.getLot().getLotNumber() + " - "
        + DateUtil.formatDate(expirationDate,
        DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR)
        + " - "
        + lotOnHandQuantity;
    final ViewGroup lotInfoView = (ViewGroup) inflater.inflate(R.layout.item_lot_info_for_stockcard, null);
    TextView txLotInfo = lotInfoView.findViewById(R.id.tx_lot_info);
    View llLotInfoBg = lotInfoView.findViewById(R.id.ll_lot_info_bg);
    boolean isExpired = lotOnHand.getLot().isExpired();
    llLotInfoBg.setBackgroundResource(isExpired ? R.drawable.lot_expired_date_bg : R.drawable.lot_unexpired_date_bg);
    txLotInfo.setTextColor(ContextCompat.getColor(getContext(), isExpired ? R.color.color_red : R.color.color_black));
    txLotInfo.setText(lotInfo);
    addView(lotInfoView, getChildCount());
  }
}
