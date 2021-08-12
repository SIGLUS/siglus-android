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
import android.widget.LinearLayout;
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
import org.openlmis.core.view.viewmodel.BulkIssueProductViewModel;
import org.openlmis.core.view.widget.SingleClickButtonListener;

public class BulkIssueAdapter extends BaseMultiItemQuickAdapter<BulkIssueProductViewModel, BaseViewHolder> {

  public BulkIssueAdapter() {
    addItemType(BulkIssueProductViewModel.TYPE_EDIT, R.layout.item_bulk_issue_edit);
    addItemType(BulkIssueProductViewModel.TYPE_DONE, R.layout.item_bulk_issue_done);
  }

  @Override
  protected void convert(@NonNull BaseViewHolder holder, BulkIssueProductViewModel viewModel) {
    holder.getView(R.id.rl_trashcan).setOnClickListener(new SingleClickButtonListener() {
      @Override
      public void onSingleClick(View v) {
        removeAt(holder.getLayoutPosition());
      }
    });

    // init lots
    RecyclerView rvLots = holder.getView(R.id.rv_lots);
    rvLots.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
    BulkIssueLotAdapter lotAdapter = new BulkIssueLotAdapter();
    rvLots.setAdapter(lotAdapter);

    if (viewModel.isDone()) {
      holder.setText(R.id.tv_product_title, viewModel.getProduct().getFormattedProductNameWithoutStrengthAndType());
      holder.getView(R.id.tv_edit).setOnClickListener(new SingleClickButtonListener() {
        @Override
        public void onSingleClick(View v) {
          viewModel.setDone(false);
          notifyItemChanged(holder.getLayoutPosition());
        }
      });
    } else {
      holder.setText(R.id.tv_product_title, TextStyleUtil.formatStyledProductName(viewModel.getProduct()));
      EditText etRequested = holder.getView(R.id.et_requested);
      etRequested.setText(viewModel.getRequested() == null ? "" : viewModel.getRequested().toString());
      SingleTextWatcher requestedTextWatcher = new SingleTextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
          viewModel.setRequested(StringUtils.isBlank(s) ? null : Long.parseLong(s.toString()));
        }
      };
      etRequested.removeTextChangedListener(requestedTextWatcher);
      etRequested.addTextChangedListener(requestedTextWatcher);
      holder.getView(R.id.tv_verified).setOnClickListener(new SingleClickButtonListener() {
        @Override
        public void onSingleClick(View v) {
          if (viewModel.validate()) {
            notifyItemChanged(holder.getLayoutPosition());
          }
        }
      });
      DividerItemDecoration decor = new DividerItemDecoration(holder.itemView.getContext(), LinearLayout.VERTICAL);
      decor.setDrawable(Objects.requireNonNull(ContextCompat
          .getDrawable(holder.itemView.getContext(), R.drawable.shape_bulk_issue_item_decoration)));
      if (rvLots.getItemDecorationCount() == 0) {
        rvLots.addItemDecoration(decor);
      }
    }
    lotAdapter.setList(viewModel.getFilteredLotViewModels());
  }
}
