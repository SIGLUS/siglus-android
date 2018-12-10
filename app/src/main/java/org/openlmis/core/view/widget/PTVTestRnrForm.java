package org.openlmis.core.view.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import org.openlmis.core.model.ProgramDataFormBasicItem;
import org.openlmis.core.view.viewmodel.PTVReportViewModel;

import java.util.List;

public class PTVTestRnrForm extends LinearLayout{

    private LayoutInflater layoutInflater;
    public PTVReportViewModel viewModel;

    public PTVTestRnrForm(Context context) {
        super(context);
        layoutInflater = LayoutInflater.from(context);

    }

    public void initView(PTVReportViewModel viewModel) {
        this.viewModel = viewModel;
    }

}
