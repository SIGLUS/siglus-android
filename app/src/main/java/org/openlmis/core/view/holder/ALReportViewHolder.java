package org.openlmis.core.view.holder;
import android.view.View;

import org.openlmis.core.view.viewmodel.ALReportItemViewModel;

public class ALReportViewHolder extends BaseViewHolder {

    private ALReportItemViewModel viewModel;

    public ALReportViewHolder(View itemView) {
        super(itemView);
    }

    public void populate(final ALReportItemViewModel alReportViewModel) {
        viewModel = alReportViewModel;
    }
}
