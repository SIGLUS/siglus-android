package org.openlmis.core.view.adapter;

import android.view.View;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.openlmis.core.R;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.adapter.IssueVoucherLotAdapter.IssueVoucherLotViewHolder;
import org.openlmis.core.view.holder.BulkEntriesLotMovementViewHolder.AmountChangeListener;
import org.openlmis.core.view.viewmodel.IssueVoucherLotViewModel;

public class IssueVoucherLotAdapter extends BaseMultiItemQuickAdapter<IssueVoucherLotViewModel,
    IssueVoucherLotViewHolder> {

  public IssueVoucherLotAdapter() {
    addItemType(IssueVoucherLotViewModel.TYPE_EDIT, R.layout.item_issue_voucher_lot_edit);
    addItemType(IssueVoucherLotViewModel.TYPE_DONE, R.layout.item_issue_voucher_lot_done);
  }


  @Override
  protected void convert(@NotNull IssueVoucherLotViewHolder holder, IssueVoucherLotViewModel viewModel) {
    holder.populate(viewModel);
  }

  protected class IssueVoucherLotViewHolder extends BaseViewHolder implements AmountChangeListener {

    private IssueVoucherLotViewModel viewModel;

    public IssueVoucherLotViewHolder(View itemView) {
      super(itemView);
    }

    @Override
    public void onAmountChange() {

    }

    public void populate(IssueVoucherLotViewModel viewModel) {
      this.viewModel = viewModel;
      setText(R.id.tv_lot_number_and_date, viewModel.getLotNumber());
      if (viewModel.isDone()) {
        setText(R.id.tv_quantity_shipped, "Quantity shipped: " + viewModel.getShippedQuantity());
        setText(R.id.tv_quantity_accepted, "Quantity accepted: " + viewModel.getAcceptedQuantity());
      } else {
        setText(R.id.et_quantity_shipped,
            viewModel.getShippedQuantity() == null ? "" : viewModel.getShippedQuantity().toString());
        setText(R.id.et_quantity_accepted,
            viewModel.getAcceptedQuantity() == null ? "" : viewModel.getAcceptedQuantity().toString());
      }
    }
  }

}
