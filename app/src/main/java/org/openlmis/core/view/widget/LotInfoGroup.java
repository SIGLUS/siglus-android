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

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.utils.DateUtil;
import org.roboguice.shaded.goole.common.collect.FluentIterable;
import org.roboguice.shaded.goole.common.collect.ImmutableList;

import java.util.Comparator;
import java.util.List;

import roboguice.RoboGuice;

public class LotInfoGroup extends org.apmem.tools.layouts.FlowLayout implements View.OnClickListener {

    LayoutInflater inflater;


    public LotInfoGroup(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
    }

    private void init(Context context) {
        inflater = LayoutInflater.from(context);

        RoboGuice.getInjector(LMISApp.getContext()).injectMembersWithoutViews(this);
    }

    public void initLotInfoGroup(List<LotOnHand> lotOnHandList) {
        removeAllViews();
        ImmutableList<LotOnHand> sortedLotOnHandList = FluentIterable.from(lotOnHandList).toSortedList(new Comparator<LotOnHand>() {
            @Override
            public int compare(LotOnHand lotOnHand1, LotOnHand lotOnHand2) {
                return lotOnHand1.getLot().getExpirationDate().compareTo(lotOnHand2.getLot().getExpirationDate());
            }
        });
        for (LotOnHand lotOnHand: sortedLotOnHandList) {
            addLotInfoView(lotOnHand);
        }
    }


    @Override
    public void onClick(View v) {
    }

    private ViewGroup addLotInfoView(LotOnHand lotOnHand) {
        String lotOnHandQuantity = "" + lotOnHand.getQuantityOnHand();
        String lotInfo = lotOnHand.getLot().getLotNumber() + " - "
                + DateUtil.formatDate(lotOnHand.getLot().getExpirationDate(), DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR)
                + " - "
                + lotOnHandQuantity;

        final ViewGroup lotInfoView = (ViewGroup) inflater.inflate(R.layout.item_lot_info_for_stockcard, null);
        TextView txLotInfo = (TextView) lotInfoView.findViewById(R.id.tx_lot_info);
        txLotInfo.setText(lotInfo);
        addView(lotInfoView, getChildCount());
        return lotInfoView;
    }

}
