package org.openlmis.core.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.openlmis.core.R;
import org.openlmis.core.view.holder.PatientDataReportRowViewHolder;
import org.openlmis.core.view.viewmodel.malaria.ImplementationReportViewModel;

import java.util.List;

import lombok.Setter;

import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

public class MalariaDataReportFormRowAdapter extends RecyclerView.Adapter<PatientDataReportRowViewHolder> {

    @Setter
    private List<ImplementationReportViewModel> viewModels;

    @Setter
    private PatientDataReportListener listener;
    private RecyclerView view;

    public MalariaDataReportFormRowAdapter() {
        viewModels = newArrayList();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        view = recyclerView;
    }


    @Override
    public PatientDataReportRowViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_patient_data_report_form_row, parent, false);
        return new PatientDataReportRowViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PatientDataReportRowViewHolder holder, int position) {
        final ImplementationReportViewModel viewModel = viewModels.get(position);
        holder.populate(viewModel);
        holder.putWatcherInComponents(patientDataTextWatcher(holder, position));
    }


    public TextWatcher patientDataTextWatcher(final PatientDataReportRowViewHolder holder, final int position) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (listener != null) {
                    listener.notifyModelChanged(holder.getImplementationViewModel());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                view.post(new Runnable() {
                    @Override
                    public void run() {
                        notifyItemChanged(viewModels.size() - 1);
                    }
                });
            }
        };
    }

    @Override
    public int getItemCount() {
        return viewModels.size();
    }

    public interface PatientDataReportListener {
        void notifyModelChanged(ImplementationReportViewModel reportViewModel);
    }
}
