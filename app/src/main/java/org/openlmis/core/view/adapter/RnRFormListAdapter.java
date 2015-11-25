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
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.repository.MMIARepository;
import org.openlmis.core.model.repository.VIARepository;
import org.openlmis.core.view.activity.MMIAActivity;
import org.openlmis.core.view.activity.RequisitionActivity;
import org.openlmis.core.view.viewmodel.RnRFormViewModel;

import java.util.List;

public class RnRFormListAdapter extends RecyclerView.Adapter<RnRFormListAdapter.ViewHolder>{

    public static final int INT_UNSET = 0;
    LayoutInflater inflater;
    Context context;

    List<RnRFormViewModel> data;
    String programCode;
    private RnRFromDeleteListener formDeleteListener;

    public RnRFormListAdapter(Context context, String programCode, List<RnRFormViewModel> data){
        this.context =context;
        this.programCode = programCode;
        inflater = LayoutInflater.from(context);
        this.data = data;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType){
            case RnRFormViewModel.TYPE_GROUP:
                return new ViewHolder(inflater.inflate(R.layout.item_rnr_list_type3, parent, false));
            case RnRFormViewModel.TYPE_DRAFT:
            case RnRFormViewModel.TYPE_UNSYNC:
                return new ViewHolder(inflater.inflate(R.layout.item_rnr_list_type1, parent, false));
            case RnRFormViewModel.TYPE_HISTORICAL:
                return new ViewHolder(inflater.inflate(R.layout.item_rnr_list_type2, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final RnRFormViewModel model = data.get(position);

        switch (getItemViewType(position)){
            case RnRFormViewModel.TYPE_GROUP:
                holder.txTitle.setText(model.getTitle());
                break;
            case RnRFormViewModel.TYPE_DRAFT:
                configHolder(holder, model.getPeriod(), Html.fromHtml(context.getString(R.string.label_incomplete_requisition, model.getName())), R.drawable.ic_description, R.color.color_draft_title);
                break;
            case RnRFormViewModel.TYPE_UNSYNC:
                configHolder(holder, model.getPeriod(), Html.fromHtml(context.getString(R.string.label_unsynced_requisition, model.getName())), R.drawable.ic_error, R.color.color_error_title);
                break;
            case RnRFormViewModel.TYPE_HISTORICAL:
                configHolder(holder, model.getPeriod(), Html.fromHtml(context.getString(R.string.label_submitted_message, model.getName(), model.getSyncedDate())), R.drawable.ic_done, INT_UNSET);

                holder.btnView.setText(context.getString(R.string.btn_view_requisition, model.getName()));
                holder.btnView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showFormDetail(model.getId());
                    }
                });
                holder.ivDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (formDeleteListener != null) {
                            formDeleteListener.delete(model.getForm());
                        }
                    }
                });
                break;
        }
    }

    public interface RnRFromDeleteListener{
        void delete(RnRForm form);
    }

    public void setItemDeleteListener(RnRFromDeleteListener formDeleteListener) {
        this.formDeleteListener = formDeleteListener;
    }

    private void configHolder(ViewHolder holder, String period, Spanned text, int icDescription, int colorDraftTitle) {
        holder.txPeriod.setText(period);
        holder.txMessage.setText(text);
        holder.icon.setImageResource(icDescription);
        if (holder.lyPeriod !=null && colorDraftTitle != INT_UNSET){
            holder.lyPeriod.setBackgroundResource(colorDraftTitle);
        }
    }

    protected void showFormDetail(long formId){
        if (MMIARepository.MMIA_PROGRAM_CODE.equals(programCode)){
            context.startActivity(MMIAActivity.getIntentToMe(context, formId));
        }else if (VIARepository.VIA_PROGRAM_CODE.equals(programCode)){
            context.startActivity(RequisitionActivity.getIntentToMe(context, formId));
        }
    }


    @Override
    public int getItemViewType(int position) {
        return data.get(position).getType();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txTitle;
        TextView txPeriod;
        TextView txMessage;
        TextView btnView;
        ImageView icon;
        ImageView ivDelete;
        View lyPeriod;

        public ViewHolder(View itemView) {
            super(itemView);

            txTitle = (TextView)itemView.findViewById(R.id.title);
            txPeriod = (TextView) itemView.findViewById(R.id.tx_period);
            txMessage = (TextView) itemView.findViewById(R.id.tx_message);
            btnView = (TextView) itemView.findViewById(R.id.btn_view);
            icon = (ImageView) itemView.findViewById(R.id.icon);
            lyPeriod = itemView.findViewById(R.id.ly_period);
            ivDelete = (ImageView) itemView.findViewById(R.id.iv_del);
        }
    }
}
