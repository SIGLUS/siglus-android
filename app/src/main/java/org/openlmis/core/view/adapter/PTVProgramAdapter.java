package org.openlmis.core.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.openlmis.core.R;
import org.openlmis.core.model.PTVProgram;
import org.openlmis.core.model.PTVProgramStockInformation;
import org.openlmis.core.model.PatientDataProgramStatus;
import org.openlmis.core.view.holder.PTVProgramViewHolder;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

public class PTVProgramAdapter extends RecyclerView.Adapter<PTVProgramViewHolder> {

    private PTVProgram ptvProgram;

    @Getter
    private List<PTVProgramStockInformation> ptvProgramStocksInformation;

    public PTVProgramAdapter(PTVProgram ptvProgram) {
        this.ptvProgram = ptvProgram;
        ptvProgramStocksInformation = new ArrayList<>();
        ptvProgramStocksInformation.addAll(ptvProgram.getPtvProgramStocksInformation());
    }

    @Override
    public PTVProgramViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (ptvProgram.getStatus().equals(PatientDataProgramStatus.SUBMITTED)) {
            return new PTVProgramViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.ptv_report_form_row, parent, false), true);
        }
        return new PTVProgramViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.ptv_report_form_row, parent, false),false);
    }

    @Override
    public void onBindViewHolder(PTVProgramViewHolder holder, int position) {
        holder.populate(ptvProgramStocksInformation.get(position));
        holder.setWatchersForEditableListeners();
    }

    public void refresh() {
        ptvProgramStocksInformation.clear();
        ptvProgramStocksInformation.addAll(ptvProgram.getPtvProgramStocksInformation());
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return ptvProgram.getPtvProgramStocksInformation().size();
    }

}
