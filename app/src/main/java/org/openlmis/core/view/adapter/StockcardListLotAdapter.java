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

package org.openlmis.core.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openlmis.core.R;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.adapter.StockcardListLotAdapter.LotInfoHolder;

public class StockcardListLotAdapter extends Adapter<LotInfoHolder> {

  List<LotOnHand> lotInfoList;

  public StockcardListLotAdapter(List<LotOnHand> lotInfoList) {
    if (lotInfoList == null) {
      return;
    }
    final ArrayList<LotOnHand> lotOnHands = new ArrayList<>(lotInfoList);
    Collections.sort(lotOnHands, (o1, o2) -> {
      final long o1ExpirationDate = o1.getLot().getExpirationDate().getTime();
      final long o2ExpirationDate = o2.getLot().getExpirationDate().getTime();
      return Long.compare(o1ExpirationDate, o2ExpirationDate);
    });
    this.lotInfoList = lotOnHands;
  }

  @NonNull
  @Override
  public LotInfoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new LotInfoHolder(
        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stockcard_list_lot_info, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull LotInfoHolder holder, int position) {
    final LotOnHand lotOnHand = lotInfoList.get(position);
    holder.lotCode.setText(lotOnHand.getLot().getLotNumber());
    holder.expiryDate.setText(DateUtil.formatDate(lotOnHand.getLot().getExpirationDate(), DateUtil.SIMPLE_DATE_FORMAT));
    holder.lotOnHand.setText(String.valueOf(lotOnHand.getQuantityOnHand()));
  }

  @Override
  public int getItemCount() {
    return lotInfoList == null ? 0 : lotInfoList.size();
  }

  static class LotInfoHolder extends ViewHolder {

    TextView lotCode;

    TextView expiryDate;

    TextView lotOnHand;

    public LotInfoHolder(@NonNull View itemView) {
      super(itemView);
      lotCode = itemView.findViewById(R.id.tv_lot_code);
      expiryDate = itemView.findViewById(R.id.tv_expiry_date);
      lotOnHand = itemView.findViewById(R.id.tv_lot_on_hand);
    }
  }
}
