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

import androidx.annotation.NonNull;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import org.openlmis.core.R;
import org.openlmis.core.view.viewmodel.BulkIssueProductViewModel;

public class BulkIssueAdapter extends BaseMultiItemQuickAdapter<BulkIssueProductViewModel, BaseViewHolder> {

  public BulkIssueAdapter() {
    addItemType(BulkIssueProductViewModel.TYPE_EDIT, R.layout.item_bulk_issue_edit);
    addItemType(BulkIssueProductViewModel.TYPE_DONE, R.layout.item_bulk_issue_edit);
  }

  @Override
  protected void convert(@NonNull BaseViewHolder holder, BulkIssueProductViewModel bulkIssueProductViewModel) {
    holder.setText(R.id.tv_temp, "data: " + bulkIssueProductViewModel.toString());
  }
}
