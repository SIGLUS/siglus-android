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

import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import org.openlmis.core.R;
import org.openlmis.core.googleanalytics.ScreenName;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.presenter.ProductPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.SelectRegimeProductAdapter;
import org.openlmis.core.view.viewmodel.RegimeProductViewModel;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Subscriber;
import rx.Subscription;

@ContentView(R.layout.activity_select_drugs)
public class SelectRegimeProductsActivity extends BaseActivity {

  public static final String PARAM_REGIME_TYPE = "regime_type";

  @InjectView(R.id.btn_next)
  public View btnNext;

  @InjectView(R.id.products_list)
  public RecyclerView productListRecycleView;

  protected SelectRegimeProductAdapter mAdapter;

  @InjectPresenter(ProductPresenter.class)
  ProductPresenter presenter;
  protected List<RegimeProductViewModel> viewModels;
  private static final int MAX_CHECKED_LIMIT = 1;

  @Override
  protected ScreenName getScreenName() {
    return ScreenName.SELECT_REGIME_PRODUCT_SCREEN;
  }

  @Override
  protected int getThemeRes() {
    return R.style.AppTheme_AMBER;
  }

  public static Intent getIntentToMe(Context context, Regimen.RegimeType type) {
    Intent intent = new Intent(context, SelectRegimeProductsActivity.class);
    intent.putExtra(PARAM_REGIME_TYPE, type);
    return intent;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    final Regimen.RegimeType regimeType = (Regimen.RegimeType) getIntent().getSerializableExtra(PARAM_REGIME_TYPE);

    productListRecycleView.setLayoutManager(new LinearLayoutManager(this));
    viewModels = new ArrayList<>();
    mAdapter = new SelectRegimeProductAdapter(viewModels);
    productListRecycleView.setAdapter(mAdapter);
    loading();
    Subscription subscription = presenter.loadRegimeProducts(regimeType).subscribe(subscriber);
    subscriptions.add(subscription);

    btnNext.setOnClickListener(v -> validateAndSaveRegime(regimeType));
  }

  private void validateAndSaveRegime(Regimen.RegimeType regimeType) {
    List<RegimeProductViewModel> checkedViewModels = getCheckedProducts();
    if (checkedViewModels.isEmpty()) {
      ToastUtil.show(R.string.hint_no_product_has_checked);
      return;
    }

    if (checkedViewModels.size() > MAX_CHECKED_LIMIT) {
      ToastUtil.show(getString(R.string.hint_more_than_limit_product_has_checked));
      return;
    }
    btnNext.setEnabled(false);
    loading();
    Subscription subscription = presenter.saveRegimes(checkedViewModels.get(0), regimeType)
        .subscribe(saveSubscriber);
    subscriptions.add(subscription);
  }


  private List<RegimeProductViewModel> getCheckedProducts() {
    return from(viewModels).filter(RegimeProductViewModel::isChecked).toList();
  }

  Subscriber<List<RegimeProductViewModel>> subscriber =
      new Subscriber<List<RegimeProductViewModel>>() {
        @Override
        public void onCompleted() {
          loaded();
          mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onError(Throwable e) {
          loaded();
          ToastUtil.show(e.getMessage());
        }

        @Override
        public void onNext(List<RegimeProductViewModel> data) {
          viewModels.clear();
          viewModels.addAll(data);
        }
      };

  Subscriber<Regimen> saveSubscriber = new Subscriber<Regimen>() {
    @Override
    public void onCompleted() {
      // do nothing
    }

    @Override
    public void onError(Throwable e) {
      loaded();
      ToastUtil.show(e.getMessage());
    }

    @Override
    public void onNext(Regimen regimen) {
      loaded();
      Intent intent = new Intent();
      intent.putExtra(Constants.PARAM_CUSTOM_REGIMEN, regimen);
      setResult(RESULT_OK, intent);
      finish();
    }
  };
}
