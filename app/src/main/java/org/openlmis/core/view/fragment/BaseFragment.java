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
import java.util.ArrayList;
import java.util.List;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.presenter.DummyPresenter;
import org.openlmis.core.presenter.Presenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.activity.BaseActivity;
import roboguice.fragment.RoboMigrationAndroidXFragment;
import rx.Subscription;

public abstract class BaseFragment extends RoboMigrationAndroidXFragment implements BaseView {

  protected boolean isSavedInstanceState;
  protected Presenter presenter;
  protected List<Subscription> subscriptions = new ArrayList<>();

  /*
   * Life cycle of Fragment: onAttach -> onCreate -> onCreateView -> onViewCreated
   * -> onActivityCreated -> onPause -> onStop
   * */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // retain this fragment
    setRetainInstance(true);
    isSavedInstanceState = false;

    setPresenter();
    attachPresenterView();
  }

  private void setPresenter() {
    presenter = initPresenter();
    if (presenter == null) {
      presenter = new DummyPresenter();
    }
  }

  private void attachPresenterView() {
    try {
      presenter.attachView(this);
    } catch (ViewNotMatchException e) {
      new LMISException(e, "BaseFragment:attachPresenterView").reportToFabric();
      ToastUtil.show(e.getMessage());
    }
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    isSavedInstanceState = true;
    super.onSaveInstanceState(outState);
  }

  public abstract Presenter initPresenter();

  @Override
  public void onStart() {
    super.onStart();
    presenter.onStart();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    presenter.onStop();
  }

  @Override
  public void loading() {
    if (getActivity() instanceof BaseActivity) {
      ((BaseActivity) getActivity()).loading();
    }
  }

  @Override
  public void loading(String message) {
    if (getActivity() instanceof BaseActivity) {
      ((BaseActivity) getActivity()).loading(message);
    }
  }

  @Override
  public void loaded() {
    if (getActivity() instanceof BaseActivity) {
      ((BaseActivity) getActivity()).loaded();
    }
  }

  protected void hideImm() {
    if (getActivity() instanceof BaseActivity) {
      ((BaseActivity) getActivity()).hideImm();
    }
  }

  @Override
  public void onDestroy() {
    unSubscribeSubscriptions();
    super.onDestroy();
  }

  private void unSubscribeSubscriptions() {
    for (Subscription subscription : subscriptions) {
      if (subscription != null) {
        subscription.unsubscribe();
      }
    }
  }
}
