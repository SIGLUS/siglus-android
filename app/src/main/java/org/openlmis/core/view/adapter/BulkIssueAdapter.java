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

import android.text.Editable;
import android.view.View;
import android.widget.EditText;
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
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.R;
import org.openlmis.core.utils.SingleTextWatcher;
import org.openlmis.core.utils.TextStyleUtil;
import org.openlmis.core.view.adapter.BulkIssueAdapter.BulkIssueProductViewHolder;
import org.openlmis.core.view.listener.AmountChangeListener;
import org.openlmis.core.view.viewmodel.BulkIssueProductViewModel;
import org.openlmis.core.view.widget.SingleClickButtonListener;

public class BulkIssueAdapter extends BaseMultiItemQuickAdapter<BulkIssueProductViewModel, BulkIssueProductViewHolder> {

  public BulkIssueAdapter() {
    addItemType(BulkIssueProductViewModel.TYPE_EDIT, R.layout.item_bulk_issue_edit);
    addItemType(BulkIssueProductViewModel.TYPE_DONE, R.layout.item_bulk_issue_done);
  }

  @Override
  protected void convert(@NonNull BulkIssueProductViewHolder holder, BulkIssueProductViewModel viewModel) {
    holder.populate(viewModel);
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

  protected class BulkIssueProductViewHolder extends BaseViewHolder implements AmountChangeListener {

    private BulkIssueProductViewModel viewModel;

    private ImageView ivTrashcan;

    private TextView tvErrorBanner;

    private TextView tvWarningBanner;

    public BulkIssueProductViewHolder(@NonNull View view) {
      super(view);
    }

    @Override
    public void onAmountChange(String amount) {
      viewModel.updateBannerRes();
      updateBanner();
    }

    public void populate(BulkIssueProductViewModel viewModel) {
      this.viewModel = viewModel;
      ivTrashcan = getView(R.id.iv_trashcan);
      getView(R.id.rl_trashcan).setOnClickListener(getRemoveClickListener());
      initLots();
      if (viewModel.isDone()) {
        setText(R.id.tv_requested, itemView.getContext().getString(R.string.label_requested)
            + ": "
            + (viewModel.getRequested() == null ? "0" : viewModel.getRequested().toString()));
        setText(R.id.tv_product_title,
            viewModel.getStockCard().getProduct().getFormattedProductNameWithoutStrengthAndType());
        getView(R.id.tv_edit).setOnClickListener(getEditClickListener());
      } else {
        tvErrorBanner = getView(R.id.tv_error_banner);
        tvWarningBanner = getView(R.id.tv_warning_banner);
        updateBanner();
        getView(R.id.tv_verified).setOnClickListener(getVerifyClickListener());
        setText(R.id.tv_product_title, TextStyleUtil.formatStyledProductName(viewModel.getStockCard().getProduct()));
        EditText etRequested = getView(R.id.et_requested);
        SingleTextWatcher requestedTextWatcher = getRequestedTextWatcher();
        etRequested.removeTextChangedListener(requestedTextWatcher);
        etRequested.addTextChangedListener(requestedTextWatcher);
        etRequested.setText(viewModel.getRequested() == null ? "" : viewModel.getRequested().toString());
      }
    }

    private void initLots() {
      // init lots
      RecyclerView rvLots = getView(R.id.rv_lots);
      rvLots.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
      BulkIssueLotAdapter lotAdapter = new BulkIssueLotAdapter();
      rvLots.setAdapter(lotAdapter);
      if (!viewModel.isDone()) {
        DividerItemDecoration decor = new DividerItemDecoration(itemView.getContext(), LinearLayout.VERTICAL);
        decor.setDrawable(Objects.requireNonNull(ContextCompat
            .getDrawable(itemView.getContext(), R.drawable.shape_bulk_issue_item_decoration)));
        if (rvLots.getItemDecorationCount() == 0) {
          rvLots.addItemDecoration(decor);
        }
      }
      lotAdapter.setAmountChangeListener(this);
      lotAdapter.setList(viewModel.getFilteredLotViewModels());
    }

    private void updateBanner() {
      int warningRes = viewModel.getWarningRes();
      if (warningRes == 0) {
        tvWarningBanner.setVisibility(View.GONE);
      } else {
        tvWarningBanner.setVisibility(View.VISIBLE);
        tvWarningBanner.setText(warningRes);
      }

      ivTrashcan.setImageResource(viewModel.shouldShowError() ? R.drawable.ic_trashcan_red : R.drawable.ic_trashcan);
      tvErrorBanner.setVisibility(viewModel.shouldShowError() ? View.VISIBLE : View.GONE);
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

    private SingleClickButtonListener getRemoveClickListener() {
      return new SingleClickButtonListener() {
        @Override
        public void onSingleClick(View v) {
          removeAt(getLayoutPosition());
        }
      };
    }

    private SingleTextWatcher getRequestedTextWatcher() {
      return new SingleTextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
          viewModel.setRequested(StringUtils.isBlank(s) ? null : Long.parseLong(s.toString()));
        }
      };
    }
  }
}
