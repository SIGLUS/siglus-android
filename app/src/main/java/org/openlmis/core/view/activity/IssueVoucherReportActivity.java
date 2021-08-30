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

package org.openlmis.core.view.activity;

import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.openlmis.core.R;
import org.openlmis.core.googleanalytics.ScreenName;
import org.openlmis.core.model.Pod;
import org.openlmis.core.presenter.IssueVoucherReportPresenter;
import org.openlmis.core.presenter.IssueVoucherReportPresenter.IssueVoucherView;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.view.adapter.IssueVoucherProductAdapter;
import org.openlmis.core.view.widget.OrderInfoView;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_issue_voucher_report)
public class IssueVoucherReportActivity extends BaseActivity implements IssueVoucherView {

  @InjectView(R.id.view_orderInfo)
  private OrderInfoView orderInfo;

  @InjectView(R.id.product_name_list_view)
  private RecyclerView rvProductList;

  @InjectView(R.id.form_list_view)
  private RecyclerView rvIssueVoucherList;

  @InjectPresenter(IssueVoucherReportPresenter.class)
  IssueVoucherReportPresenter presenter;

  private long formId;
  private Pod pod;
  private IssueVoucherProductAdapter productAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    formId = getIntent().getLongExtra(Constants.PARAM_ISSUE_VOUCHER_FORM_ID, 1);
    pod = (Pod) getIntent().getExtras().getSerializable(Constants.PARAM_ISSUE_VOUCHER);
    rvProductList.setLayoutManager(new LinearLayoutManager(this));
    productAdapter = new IssueVoucherProductAdapter();
    rvProductList.setAdapter(productAdapter);
    rvIssueVoucherList.setLayoutManager(new LinearLayoutManager(this));
    if (pod != null) {
      refreshIssueVoucherForm(pod);
    } else {
      presenter.loadData(formId);
    }
  }

  @Override
  protected ScreenName getScreenName() {
    return ScreenName.ISSUE_VOUCHER_REPORT_SCREEN;
  }

  @Override
  public void refreshIssueVoucherForm(Pod pod) {
    orderInfo.refresh(pod);
    productAdapter.setList(presenter.getIssueVoucherReportViewModel().getProductViewModels());
  }
}
