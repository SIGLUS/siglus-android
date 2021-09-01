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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.inject.Inject;
import lombok.AccessLevel;
import lombok.Setter;
import org.openlmis.core.R;
import org.openlmis.core.constant.IntentConstants;
import org.openlmis.core.enumeration.OrderStatus;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Pod;
import org.openlmis.core.presenter.IssueVoucherListPresenter;
import org.openlmis.core.presenter.IssueVoucherListPresenter.IssueVoucherListView;
import org.openlmis.core.presenter.Presenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.activity.EditOrderNumberActivity;
import org.openlmis.core.view.activity.IssueVoucherReportActivity;
import org.openlmis.core.view.adapter.IssueVoucherListAdapter;
import org.openlmis.core.view.listener.OrderOperationListener;
import roboguice.inject.InjectView;

public class IssueVoucherListFragment extends BaseFragment implements IssueVoucherListView, OrderOperationListener {

  @InjectView(R.id.rv_issue_voucher)
  private RecyclerView rvIssueVoucher;

  @Setter(AccessLevel.PROTECTED)
  private IssueVoucherListAdapter adapter;

  @Inject
  private IssueVoucherListPresenter presenter;

  private final ActivityResultLauncher<Intent> editOrderNumberResultLauncher = registerForActivityResult(
      new StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
          ToastUtil.show("edit order number success");
        }
      }
  );

  public static IssueVoucherListFragment newInstance(boolean isIssueVoucher) {
    final IssueVoucherListFragment issueVoucherListFragment = new IssueVoucherListFragment();
    final Bundle params = new Bundle();
    params.putBoolean(IntentConstants.PARAM_IS_ISSUE_VOUCHER, isIssueVoucher);
    issueVoucherListFragment.setArguments(params);
    return issueVoucherListFragment;
  }

  @Override
  public Presenter initPresenter() {
    return presenter;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_issue_voucher_list, container, false);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    rvIssueVoucher.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
    adapter = new IssueVoucherListAdapter();
    adapter.setListener(this);
    adapter.setNewInstance(presenter.getViewModels());
    if (adapter.getHeaderLayoutCount() == 0 && adapter.getFooterLayoutCount() == 0) {
      adapter.addHeaderView(generateHeaderView());
      adapter.addFooterView(generateHeaderView());
    }
    rvIssueVoucher.setAdapter(adapter);
    presenter.setIssueVoucher(requireArguments().getBoolean(IntentConstants.PARAM_IS_ISSUE_VOUCHER));
    presenter.loadData();
  }

  private View generateHeaderView() {
    View view = new View(requireContext());
    LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
        requireActivity().getResources().getDimensionPixelOffset(R.dimen.px_16));
    view.setLayoutParams(layoutParams);
    return view;
  }

  @Override
  public void onRefreshList() {
    adapter.notifyDataSetChanged();
  }

  @Override
  public void onLoadDataFailed(LMISException lmisException) {
    ToastUtil.show(lmisException.getMsg());
    requireActivity().finish();
  }

  @Override
  public void orderDeleteOrEditOperation(OrderStatus orderStatus, String orderCode) {
    if (OrderStatus.SHIPPED == orderStatus) {
      SimpleDialogFragment dialogFragment = SimpleDialogFragment.newInstance(
          null,
          getString(R.string.msg_issue_voucher_remove_confirm),
          getString(R.string.btn_positive),
          getString(R.string.btn_negative),
          null);
      dialogFragment.show(getParentFragmentManager(), "delete_issue_voucher_confirm_dialog");
      dialogFragment.setCallBackListener(new SimpleDialogFragment.MsgDialogCallBack() {
        @Override
        public void positiveClick(String tag) {
          presenter.deleteIssueVoucher(orderCode);
        }

        @Override
        public void negativeClick(String tag) {
          // do nothing
        }
      });
    } else {
      Intent intent = new Intent(requireContext(), EditOrderNumberActivity.class);
      intent.putExtra(IntentConstants.PARAM_ORDER_NUMBER, orderCode);
      editOrderNumberResultLauncher.launch(intent);
    }
  }

  @Override
  public void orderEditOrViewOperation(Pod pod) {
    Intent intent = new Intent(getActivity(), IssueVoucherReportActivity.class);
    intent.putExtra(Constants.PARAM_ISSUE_VOUCHER_FORM_ID, pod.getId());
    startActivity(intent);
  }
}
