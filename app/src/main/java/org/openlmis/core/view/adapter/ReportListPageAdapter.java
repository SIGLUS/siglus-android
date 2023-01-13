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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import java.util.List;
import org.openlmis.core.model.ReportTypeForm;
import org.openlmis.core.view.fragment.ReportListFragment;

public class ReportListPageAdapter extends FragmentStateAdapter {

  private List<ReportTypeForm> data;

  public ReportListPageAdapter(@NonNull FragmentActivity fragmentActivity) {
    super(fragmentActivity);
  }

  public void setData(List<ReportTypeForm> data) {
    this.data = data;
    notifyItemRangeChanged(0, data.size() - 1);
  }

  @NonNull
  @Override
  public Fragment createFragment(int position) {
    return ReportListFragment.newInstance(data.get(position).getCode());
  }

  @Override
  public int getItemCount() {
    return data == null ? 0 : data.size();
  }
}
