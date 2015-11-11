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

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.openlmis.core.R;
import org.openlmis.core.view.holder.RnRFormViewHolder;
import org.openlmis.core.view.viewmodel.RnRFormViewModel;

import java.util.List;

public class RnRFormListAdapter extends RecyclerView.Adapter<RnRFormViewHolder> {

    private LayoutInflater inflater;
    private List<RnRFormViewModel> data;
    private String programCode;

    public RnRFormListAdapter(Context context, String programCode, List<RnRFormViewModel> data) {
        this.inflater = LayoutInflater.from(context);
        this.programCode = programCode;
        this.data = data;
    }

    public void refreshList(List<RnRFormViewModel> data) {
        this.data.clear();
        this.data.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public RnRFormViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case RnRFormViewModel.TYPE_GROUP:
                return new RnRFormViewHolder(inflater.inflate(R.layout.item_rnr_list_type3, parent, false));
            case RnRFormViewModel.TYPE_DRAFT:
            case RnRFormViewModel.TYPE_UNSYNC:
                return new RnRFormViewHolder(inflater.inflate(R.layout.item_rnr_list_type1, parent, false));
            case RnRFormViewModel.TYPE_HISTORICAL:
                return new RnRFormViewHolder(inflater.inflate(R.layout.item_rnr_list_type2, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RnRFormViewHolder holder, int position) {
        final RnRFormViewModel model = data.get(position);

        holder.populate(model, programCode);
    }

    @Override
    public int getItemViewType(int position) {
        return data.get(position).getType();
    }
}
