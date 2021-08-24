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

package org.openlmis.core.view.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import org.openlmis.core.constant.IntentConstants;
import org.openlmis.core.presenter.IssueVoucherListPresenter;
import org.openlmis.core.presenter.Presenter;

public class IssueVoucherListFragment extends BaseFragment {

  public static IssueVoucherListFragment newInstance(boolean isIssueVoucher) {
    final IssueVoucherListFragment issueVoucherListFragment = new IssueVoucherListFragment();
    final Bundle params = new Bundle();
    params.putBoolean(IntentConstants.PARAM_IS_ISSUE_VOUCHER, isIssueVoucher);
    issueVoucherListFragment.setArguments(params);
    return issueVoucherListFragment;
  }

  @Override
  public Presenter initPresenter() {
    return new IssueVoucherListPresenter();
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    boolean isIssueVoucher = requireArguments().getBoolean(IntentConstants.PARAM_IS_ISSUE_VOUCHER);
    Log.d("TAG", " isIssueVoucher = " + isIssueVoucher);
  }
}
