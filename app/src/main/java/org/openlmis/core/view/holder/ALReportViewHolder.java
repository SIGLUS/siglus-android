package org.openlmis.core.view.holder;
import android.view.View;

import org.openlmis.core.view.viewmodel.ALReportViewModel;
import org.openlmis.core.view.viewmodel.RapidTestReportViewModel;

public class ALReportViewHolder extends BaseViewHolder {

    private ALReportViewModel viewModel;

    public ALReportViewHolder(View itemView) {
        super(itemView);
    }

    public void populate(final ALReportViewModel alReportViewModel) {
        viewModel = alReportViewModel;
    }
}
