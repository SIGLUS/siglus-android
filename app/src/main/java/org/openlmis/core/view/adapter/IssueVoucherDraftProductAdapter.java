package org.openlmis.core.view.adapter;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
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
import org.openlmis.core.enumeration.IssueVoucherValidationType;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.TextStyleUtil;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.activity.BaseActivity;
import org.openlmis.core.view.adapter.IssueVoucherDraftProductAdapter.IssueVoucherProductViewHolder;
import org.openlmis.core.view.listener.AmountChangeListener;
import org.openlmis.core.view.listener.OnRemoveListener;
import org.openlmis.core.view.viewmodel.IssueVoucherLotViewModel;
import org.openlmis.core.view.viewmodel.IssueVoucherProductViewModel;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.openlmis.core.view.widget.AddLotDialogFragment;
import org.openlmis.core.view.widget.BaseLotListView.OnDismissListener;
import org.openlmis.core.view.widget.SingleClickButtonListener;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

public class IssueVoucherDraftProductAdapter extends BaseMultiItemQuickAdapter<IssueVoucherProductViewModel,
    IssueVoucherProductViewHolder> {

  @Setter
  private OnRemoveListener removeListener;

  private RecyclerView rvLots;

  public IssueVoucherDraftProductAdapter() throws NoSuchMethodError{
    addItemType(IssueVoucherProductViewModel.TYPE_EDIT, R.layout.item_issue_voucher_draft_edit);
    addItemType(IssueVoucherProductViewModel.TYPE_DONE, R.layout.item_issue_voucher_draft_done);
  }

  public int validateAll() {
    int position = -1;
    for (int i = 0; i < getData().size(); i++) {
      if (getData().get(i).validate()) {
        continue;
      }
      if (position == -1) {
        position = i;
      }
    }
    notifyDataSetChanged();
    return position;
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

    public IssueVoucherProductViewHolder(@NotNull View view) {
      super(view);
    }

    @Override
    public void onAmountChange(String value) {
      viewModel.validProduct();
      updateErrorStatus();
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
        tvErrorBanner = getView(R.id.tv_error_banner);
        getView(R.id.btn_verify).setOnClickListener(getVerifyClickListener());
        setText(R.id.tv_product_title, TextStyleUtil.formatStyledProductName(viewModel.getProduct()));
        updateErrorStatus();
      }
    }

    private void setAllLotShouldShowError() {
      for (IssueVoucherLotViewModel lotViewModel : viewModel.getLotViewModels()) {
        lotViewModel.setShouldShowError(true);
      }
    }

    private void initLots() {
      rvLots = getView(R.id.rv_lots);
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
      lotAdapter.setAmountChangeListener(this);
      List<IssueVoucherLotViewModel> filteredLotViewModels = viewModel.getLotViewModels();
      if (viewModel.isDone()) {
        filteredLotViewModels = FluentIterable.from(viewModel.getLotViewModels()).filter(issueVoucherLotViewModel ->
            Objects.requireNonNull(issueVoucherLotViewModel).getShippedQuantity() != null).toList();
      }
      lotAdapter.setNewInstance(filteredLotViewModels);
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
          viewModel.validate();
          notifyItemChanged(getLayoutPosition());
          setAllLotShouldShowError();
          updateErrorStatus();
          rvLots.requestFocus();
        }
      };
    }

    private void updateErrorStatus() {
      if (IssueVoucherValidationType.NO_LOT == viewModel.getValidationType()) {
        ivTrashcan.setImageResource(R.drawable.ic_red_ashcan);
        tvErrorBanner.setVisibility(View.VISIBLE);
        tvErrorBanner.setText(R.string.alert_issue_voucher_can_not_be_blank);
        btnAddNewLot.setBackground(ResourcesCompat.getDrawable(getContext().getResources(),
            R.drawable.border_round_red, null));
        btnAddNewLot.setTextColor(ContextCompat.getColor(getContext(), R.color.color_red));
      } else if (IssueVoucherValidationType.ALL_LOT_BLANK == viewModel.getValidationType()) {
        ivTrashcan.setImageResource(R.drawable.ic_red_ashcan);
        tvErrorBanner.setVisibility(View.VISIBLE);
        tvErrorBanner.setText(R.string.alert_issue_voucher_can_not_be_blank);
        btnAddNewLot.setTextColor(ContextCompat.getColor(getContext(), R.color.color_accent));
        btnAddNewLot.setBackground(ResourcesCompat.getDrawable(getContext().getResources(),
            R.drawable.border_round_blue, null));
      } else {
        ivTrashcan.setImageResource(R.drawable.ic_ashcan);
        tvErrorBanner.setVisibility(View.GONE);
        btnAddNewLot.setTextColor(ContextCompat.getColor(getContext(), R.color.color_accent));
        btnAddNewLot.setBackground(ResourcesCompat.getDrawable(getContext().getResources(),
            R.drawable.border_round_blue, null));
      }

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
                    addLotDialogFragment.getExpiryDate(), viewModel.getProduct()));
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
          addNewLot(new IssueVoucherLotViewModel(lotNumber, expiryDate, viewModel.getProduct()));
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
      lotAdapter.notifyItemInserted(lotAdapter.getItemPosition(issueVoucherLotViewModel));
      viewModel.validProduct();
      issueVoucherLotViewModel.setShouldShowError(false);
      updateErrorStatus();
    }
  }

}
