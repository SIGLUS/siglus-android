/*
 * This program is part of the OpenLMIS logistics management information
 * system platform software.
 *
 * Copyright © 2015 ThoughtWorks, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should
 * have received a copy of the GNU Affero General Public License along with
 * this program. If not, see http://www.gnu.org/licenses. For additional
 * information contact info@OpenLMIS.org
 */

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
import org.openlmis.core.view.listener.OnUpdatePodListener;
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
  private OnUpdatePodListener removeListener;

  public IssueVoucherDraftProductAdapter() {
    addItemType(IssueVoucherProductViewModel.TYPE_EDIT, R.layout.item_issue_voucher_draft_edit);
    addItemType(IssueVoucherProductViewModel.TYPE_DONE, R.layout.item_issue_voucher_draft_done);
  }

  public int validateAll() {
    int position = -1;
    for (int i = 0; i < getData().size(); i++) {
      if (getData().get(i).validate()) {
        continue;
      }
      getData().get(i).setShouldShowError(true);
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

    protected AddLotDialogFragment addLotDialogFragment;

    private IssueVoucherProductViewModel viewModel;

    private IssueVoucherLotAdapter lotAdapter;

    private static final String ADD_LOT = "add_new_lot";

    private ImageView ivTrashcan;

    private TextView tvErrorBanner;

    private TextView btnAddNewLot;

    private RecyclerView rvLots;

    public IssueVoucherProductViewHolder(@NotNull View view) {
      super(view);
    }

    @Override
    public void onAmountChange(String value) {
      viewModel.validProduct();
      viewModel.setShouldShowError(true);
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
        if (viewModel.isShouldShowError()) {
          updateErrorStatus();
        } else {
          setValidStatus();
        }
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
        rvLots.addItemDecoration(new DividerItemDecoration(itemView.getContext(), DividerItemDecoration.VERTICAL));
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
          viewModel.setShouldShowError(true);
          setAllLotShouldShowError();
          notifyItemChanged(getLayoutPosition());
          updateErrorStatus();
          rvLots.requestFocus();
        }
      };
    }

    private void setAllLotShouldShowError() {
      for (IssueVoucherLotViewModel lotViewModel : viewModel.getLotViewModels()) {
        lotViewModel.setShouldShowError(true);
      }
    }

    private void updateErrorStatus() {
      if (IssueVoucherValidationType.NO_LOT == viewModel.getValidationType()) {
        setNoLotStatus();
      } else if (IssueVoucherValidationType.ALL_LOT_BLANK == viewModel.getValidationType()) {
        setAllLotBlankStatus();
      } else {
        setValidStatus();
      }
    }

    private void setNoLotStatus() {
      ivTrashcan.setImageResource(R.drawable.ic_red_ashcan);
      tvErrorBanner.setVisibility(View.VISIBLE);
      tvErrorBanner.setText(R.string.alert_issue_voucher_can_not_be_blank);
      btnAddNewLot.setBackground(ResourcesCompat.getDrawable(getContext().getResources(),
          R.drawable.border_round_red, null));
      btnAddNewLot.setTextColor(ContextCompat.getColor(getContext(), R.color.color_red));
    }

    private void setAllLotBlankStatus() {
      ivTrashcan.setImageResource(R.drawable.ic_red_ashcan);
      tvErrorBanner.setVisibility(View.VISIBLE);
      tvErrorBanner.setText(R.string.alert_issue_voucher_can_not_be_blank);
      btnAddNewLot.setTextColor(ContextCompat.getColor(getContext(), R.color.color_accent));
      btnAddNewLot.setBackground(ResourcesCompat.getDrawable(getContext().getResources(),
          R.drawable.border_round_blue, null));
    }

    private void setValidStatus() {
      ivTrashcan.setImageResource(R.drawable.ic_ashcan);
      tvErrorBanner.setVisibility(View.GONE);
      btnAddNewLot.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.color_accent));
      btnAddNewLot.setBackground(ResourcesCompat.getDrawable(itemView.getContext().getResources(),
          R.drawable.border_round_blue, null));
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
      return this::setActionAddNewEnabled;
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
      return new ArrayList<>(FluentIterable.from(viewModel.getLotViewModels())
          .transform(IssueVoucherLotViewModel::getLotNumber).toList());
    }

    private void setActionAddNewEnabled() {
      btnAddNewLot.setEnabled(true);
    }

    private void addNewLot(IssueVoucherLotViewModel issueVoucherLotViewModel) {
      viewModel.getLotViewModels().add(issueVoucherLotViewModel);
      lotAdapter.notifyItemInserted(lotAdapter.getItemPosition(issueVoucherLotViewModel));
      viewModel.validProduct();
      issueVoucherLotViewModel.setShouldShowError(false);
      if (viewModel.isShouldShowError()) {
        updateErrorStatus();
      }
    }
  }

}
