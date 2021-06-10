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
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import org.openlmis.core.R;

public class RapidTestTopProductCodeAdapter extends
    RecyclerView.Adapter<RapidTestTopProductCodeAdapter.RapidTestTopProductCodeViewHolder> {

  private final List<String> productCodes;

  public RapidTestTopProductCodeAdapter(List<String> productCodes) {
    this.productCodes = productCodes;
  }

  @Override
  public RapidTestTopProductCodeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    final View inflate = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_rapid_test_top_left, parent, false);
    return new RapidTestTopProductCodeViewHolder(inflate);
  }

  @Override
  public void onBindViewHolder(RapidTestTopProductCodeViewHolder holder, int position) {
    holder.tvProductCode.setText(productCodes.get(position));
  }

  @Override
  public int getItemCount() {
    return productCodes == null ? 0 : productCodes.size();
  }

  protected static class RapidTestTopProductCodeViewHolder extends RecyclerView.ViewHolder {

    TextView tvProductCode;

    public RapidTestTopProductCodeViewHolder(View itemView) {
      super(itemView);
      tvProductCode = itemView.findViewById(R.id.left_tv_code);
    }
  }
}
