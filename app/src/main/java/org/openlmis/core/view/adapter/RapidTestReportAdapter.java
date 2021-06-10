package org.openlmis.core.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import org.openlmis.core.R;
import org.openlmis.core.view.holder.RapidTestReportViewHolder;
import org.openlmis.core.view.viewmodel.RapidTestReportViewModel;

public class RapidTestReportAdapter extends RecyclerView.Adapter<RapidTestReportViewHolder> {

  private final Context context;
  private final List<RapidTestReportViewModel> viewModels;

  public RapidTestReportAdapter(Context context, List<RapidTestReportViewModel> viewModels) {
    this.context = context;
    this.viewModels = viewModels;
  }

  @Override
  public RapidTestReportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    if (viewType == RapidTestReportViewModel.Status.COMPLETED.getViewType()) {
      return new RapidTestReportViewHolder(
          LayoutInflater.from(context).inflate(R.layout.item_report_no_button, parent, false));
    }
    return new RapidTestReportViewHolder(
        LayoutInflater.from(context).inflate(R.layout.item_rapid_test_report, parent, false));
  }

  @Override
  public void onBindViewHolder(RapidTestReportViewHolder holder, int position) {
    holder.populate(viewModels.get(position));
  }

  @Override
  public int getItemCount() {
    return viewModels.size();
  }

  @Override
  public int getItemViewType(int position) {
    return viewModels.get(position).getStatus().getViewType();
  }
}
