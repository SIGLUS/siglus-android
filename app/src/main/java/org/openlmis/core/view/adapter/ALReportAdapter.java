package org.openlmis.core.view.adapter;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.openlmis.core.R;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.view.holder.ALReportViewHolder;
import org.openlmis.core.view.viewmodel.ALReportItemViewModel;
import org.openlmis.core.view.viewmodel.ALReportViewModel;

public class ALReportAdapter extends RecyclerView.Adapter<ALReportViewHolder> {
    private ALReportViewModel alReportViewModel;
    private ALReportViewHolder.QuantityChangeListener quantityChangeListener;

    public ALReportAdapter(ALReportViewHolder.QuantityChangeListener quantityChangeListener) {
        alReportViewModel = new ALReportViewModel();
        this.quantityChangeListener = quantityChangeListener;
    }

    @Override
    public ALReportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_al_report_row, parent, false);
        return new ALReportViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ALReportViewHolder holder, int position) {
        ALReportItemViewModel viewModel = alReportViewModel.getItemViewModelList().get(position);
        holder.populate(viewModel, quantityChangeListener, getFormStatus(alReportViewModel));
    }

    @Override
    public int getItemCount() {
        return alReportViewModel.getItemViewModelList().size();
    }

    private boolean getFormStatus(ALReportViewModel alReportViewModel) {
        return alReportViewModel != null
                && alReportViewModel.getForm() != null
                && (alReportViewModel.getForm().getStatus() == RnRForm.STATUS.AUTHORIZED
                || alReportViewModel.getForm().getStatus() == RnRForm.STATUS.SUBMITTED);
    }

    public void updateTotal() {
        notifyItemChanged(getItemCount() - 1);
    }

    public void updateTip() {
        notifyItemChanged(0);
        notifyItemChanged(1);

    }

    public void refresh(ALReportViewModel viewModel) {
        alReportViewModel = viewModel;
        notifyDataSetChanged();
    }
}
