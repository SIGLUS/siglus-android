package org.openlmis.core.view.adapter;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.TextStyleUtil;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.activity.BaseActivity;
import org.openlmis.core.view.adapter.IssueVoucherDraftProductAdapter.IssueVoucherProductViewHolder;
import org.openlmis.core.view.holder.BulkEntriesLotMovementViewHolder.AmountChangeListener;
import org.openlmis.core.view.listener.OnRemoveListener;
import org.openlmis.core.view.viewmodel.IssueVoucherLotViewModel;
import org.openlmis.core.view.viewmodel.IssueVoucherProductViewModel;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.openlmis.core.view.widget.AddLotDialogFragment;
import org.openlmis.core.view.widget.BaseLotListView.OnDismissListener;
import org.openlmis.core.view.widget.SingleClickButtonListener;
import org.roboguice.shaded.goole.common.collect.FluentIterable;
import org.w3c.dom.Text;

public class IssueVoucherDraftProductAdapter extends BaseMultiItemQuickAdapter<IssueVoucherProductViewModel,
    IssueVoucherProductViewHolder> {

  @Setter
  private OnRemoveListener removeListener;

  public IssueVoucherDraftProductAdapter() throws NoSuchMethodError{
    addItemType(IssueVoucherProductViewModel.TYPE_EDIT, R.layout.item_issue_voucher_draft_edit);
    addItemType(IssueVoucherProductViewModel.TYPE_DONE, R.layout.item_issue_voucher_draft_done);
  }

  @Override
  protected void convert(@NotNull IssueVoucherProductViewHolder holder, IssueVoucherProductViewModel viewModel) {
    holder.populate(viewModel);
  }

  protected class IssueVoucherProductViewHolder extends BaseViewHolder implements AmountChangeListener {

    private IssueVoucherProductViewModel viewModel;

    private IssueVoucherLotAdapter lotAdapter;

    protected AddLotDialogFragment addLotDialogFragment;

    public static final String ADD_LOT = "add_new_lot";

    private ImageView ivTrashcan;

    private TextView tvErrorBanner;

    private TextView btnAddNewLot;

    private TextView btnVerify;

    public IssueVoucherProductViewHolder(@NotNull View view) {
      super(view);
    }

    @Override
    public void onAmountChange() {

    }

    public void populate(IssueVoucherProductViewModel viewModel) {
      this.viewModel = viewModel;
      ivTrashcan = getView(R.id.iv_trashcan);
      ivTrashcan.setOnClickListener(getRemoveClickListener());
      initLots();
      if (viewModel.isDone()) {
        getView(R.id.tv_edit).setOnClickListener(getEditClickListener());
        setText(R.id.tv_product_title, viewModel.getProduct().getFormattedProductNameWithoutStrengthAndType());
      } else {
        btnAddNewLot = getView(R.id.btn_add_new_lot);
        btnAddNewLot.setOnClickListener(getAddNewLotOnClickListener());
        getView(R.id.btn_verify).setOnClickListener(getVerifyClickListener());
        setText(R.id.tv_product_title, TextStyleUtil.formatStyledProductName(viewModel.getStockCard().getProduct()));
      }
    }

    private void initLots() {
      RecyclerView rvLots = getView(R.id.rv_lots);
      rvLots.setLayoutManager(new LinearLayoutManager(itemView.getContext()) {
        @Override
        public boolean canScrollVertically() {
          return false;
        }
      });
      lotAdapter = new IssueVoucherLotAdapter();
      rvLots.setAdapter(lotAdapter);
      if (rvLots.getItemDecorationCount() == 0) {
        rvLots.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
      }
      lotAdapter.setNewInstance(viewModel.getLotViewModels());
    }

    private SingleClickButtonListener getRemoveClickListener() {
      return new SingleClickButtonListener() {
        @Override
        public void onSingleClick(View v) {
          if (removeListener != null) {
            removeListener.onRemove(getLayoutPosition());
          }
        }
      };
    }

    private SingleClickButtonListener getEditClickListener() {
      return new SingleClickButtonListener() {
        @Override
        public void onSingleClick(View v) {
          viewModel.setDone(false);
          notifyItemChanged(getLayoutPosition());
        }
      };
    }

    private SingleClickButtonListener getVerifyClickListener() {
      return new SingleClickButtonListener() {
        @Override
        public void onSingleClick(View v) {
          if (viewModel.validate()) {
            notifyItemChanged(getLayoutPosition());
          } else {
            updateBanner();
          }
        }
      };
    }

    private void updateBanner() {
      int errorRes = viewModel.getErrorRes();
      if (errorRes == 0) {
        tvErrorBanner.setVisibility(View.GONE);
      } else {
        tvErrorBanner.setVisibility(View.VISIBLE);
        tvErrorBanner.setText(errorRes);
      }

      ivTrashcan.setImageResource(viewModel.isShouldShowError() ? R.drawable.ic_trashcan_red : R.drawable.ic_trashcan);
      tvErrorBanner.setVisibility(viewModel.isShouldShowError() ? View.VISIBLE : View.GONE);
    }

    @NonNull
    private SingleClickButtonListener getAddNewLotOnClickListener() {
      return new SingleClickButtonListener() {
        @Override
        public void onSingleClick(View v) {
          showAddLotDialogFragment();
        }
      };
    }

    private boolean showAddLotDialogFragment() {
      Bundle bundle = new Bundle();
      bundle.putString(Constants.PARAM_STOCK_NAME, viewModel.getProduct().getFormattedProductName());
      addLotDialogFragment = new AddLotDialogFragment();
      addLotDialogFragment.setArguments(bundle);
      addLotDialogFragment.setListener(getAddNewLotDialogOnClickListener());
      addLotDialogFragment.setOnDismissListener(getOnAddNewLotDialogDismissListener());
      addLotDialogFragment.setAddLotWithoutNumberListener(getAddLotWithoutNumberListener());
      addLotDialogFragment.show(((BaseActivity) getContext()).getSupportFragmentManager(), ADD_LOT);
      return true;
    }

    @NonNull
    private SingleClickButtonListener getAddNewLotDialogOnClickListener() {
      return new SingleClickButtonListener() {
        @Override
        public void onSingleClick(View v) {
          switch (v.getId()) {
            case R.id.btn_complete:
              if (addLotDialogFragment.validate() && !addLotDialogFragment.hasIdenticalLot(getLotNumbers())) {
                addNewLot(new IssueVoucherLotViewModel(addLotDialogFragment.getLotNumber(),
                    addLotDialogFragment.getExpiryDate()));
                addLotDialogFragment.dismiss();
              }
              break;
            case R.id.btn_cancel:
              addLotDialogFragment.dismiss();
              break;
            default:
              // do nothing
          }
        }
      };
    }

    @NonNull
    private OnDismissListener getOnAddNewLotDialogDismissListener() {
      return () -> setActionAddNewEnabled(true);
    }

    private AddLotDialogFragment.AddLotWithoutNumberListener getAddLotWithoutNumberListener() {
      return expiryDate -> {
        btnAddNewLot.setEnabled(true);
        String lotNumber = LotMovementViewModel
            .generateLotNumberForProductWithoutLot(viewModel.getProduct().getCode(), expiryDate);
        if (getLotNumbers().contains(lotNumber)) {
          ToastUtil.show(LMISApp.getContext().getString(R.string.error_lot_without_number_already_exists));
        } else {
          addNewLot(new IssueVoucherLotViewModel(lotNumber, expiryDate));
        }
      };
    }

    @NonNull
    private List<String> getLotNumbers() {
      final List<String> existingLots = new ArrayList<>();
      existingLots.addAll(FluentIterable.from(viewModel.getLotViewModels())
          .transform(IssueVoucherLotViewModel::getLotNumber).toList());
      return existingLots;
    }

    private void setActionAddNewEnabled(boolean actionAddNewEnabled) {
      btnAddNewLot.setEnabled(actionAddNewEnabled);
    }

    private void addNewLot(IssueVoucherLotViewModel issueVoucherLotViewModel) {
      viewModel.getLotViewModels().add(issueVoucherLotViewModel);
      lotAdapter.notifyDataSetChanged();
    }
  }

}
