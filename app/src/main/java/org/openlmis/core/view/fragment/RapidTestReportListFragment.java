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
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.inject.Inject;
import java.util.List;
import java.util.Objects;
import org.openlmis.core.R;
import org.openlmis.core.model.Period;
import org.openlmis.core.presenter.Presenter;
import org.openlmis.core.presenter.RapidTestReportsPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.activity.RapidTestReportFormActivity;
import org.openlmis.core.view.adapter.RapidTestReportAdapter;
import org.openlmis.core.view.viewmodel.RapidTestReportViewModel;
import roboguice.inject.InjectView;
import rx.Subscriber;
import rx.Subscription;

public class RapidTestReportListFragment extends BaseReportListFragment {

  public static RapidTestReportListFragment newInstance() {
    return new RapidTestReportListFragment();
  }

  @InjectView(R.id.rv_rapid_test_list)
  RecyclerView rapidTestListView;

  @Inject
  RapidTestReportsPresenter presenter;

  private RapidTestReportAdapter rapidTestReportAdapter;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_rapid_test_report_list, container, false);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    rapidTestListView.setLayoutManager(new LinearLayoutManager(requireContext()));
    rapidTestReportAdapter = new RapidTestReportAdapter(this, presenter.getViewModelList());
    rapidTestListView.setAdapter(rapidTestReportAdapter);

    loadForms();
  }

  @Override
  public Presenter initPresenter() {
    return presenter;
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == Constants.REQUEST_CREATE_OR_MODIFY_RAPID_TEST_FORM
        || requestCode == Constants.REQUEST_FROM_RNR_LIST_PAGE) {
      loadForms();
    } else if (requestCode == Constants.REQUEST_SELECT_PERIOD_END && resultCode == Activity.RESULT_OK && data != null) {
      Period period = (Period) data.getSerializableExtra(Constants.PARAM_PERIOD);
      startActivityForResult(RapidTestReportFormActivity
              .getIntentToMe(requireContext(), RapidTestReportViewModel.DEFAULT_FORM_ID, period,
                  Objects.requireNonNull(period).getBegin()),
          Constants.REQUEST_CREATE_OR_MODIFY_RAPID_TEST_FORM);
    }
  }

  @Override
  protected void loadForms() {
    loading();
    Subscription subscription = presenter.loadViewModels().subscribe(getRefreshReportListSubscriber());
    subscriptions.add(subscription);
  }

  private Subscriber<List<RapidTestReportViewModel>> getRefreshReportListSubscriber() {
    return new Subscriber<List<RapidTestReportViewModel>>() {
      @Override
      public void onCompleted() {
        loaded();
      }

      @Override
      public void onError(Throwable e) {
        loaded();
        ToastUtil.show(e.getMessage());
      }

      @Override
      public void onNext(List<RapidTestReportViewModel> rapidTestReportViewModels) {
        rapidTestReportAdapter.notifyDataSetChanged();
      }
    };
  }
}