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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.openlmis.core.R;
import org.openlmis.core.googleanalytics.ScreenName;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.presenter.AddDrugsToVIAPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.AddDrugsToVIAAdapter;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;

@SuppressWarnings("squid:S110")
@ContentView(R.layout.activity_add_drugs_to_via)
public class AddDrugsToVIAActivity extends SearchBarActivity {

  @InjectView(R.id.btn_complete)
  public View btnComplete;

  @InjectView(R.id.products_list)
  public RecyclerView productListRecycleView;

  @InjectPresenter(AddDrugsToVIAPresenter.class)
  AddDrugsToVIAPresenter presenter;

  protected AddDrugsToVIAAdapter mAdapter;
  private Date periodBegin;

  @Override
  protected ScreenName getScreenName() {
    return ScreenName.ADD_DRUGS_TO_VIA_SCREEN;
  }

  @Override
  protected int getThemeRes() {
    return R.style.AppTheme_PURPLE;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    periodBegin = ((Date) getIntent().getSerializableExtra(Constants.PARAM_PERIOD_BEGIN));
    List<String> existingAdditionalProductList = (List<String>) getIntent()
        .getSerializableExtra(Constants.PARAM_ADDED_DRUG_CODES_IN_VIA);

    initRecyclerView();
    loading();
    Subscription subscription = presenter
        .loadActiveProductsNotInVIAForm(existingAdditionalProductList).subscribe(subscriber);
    subscriptions.add(subscription);

    btnComplete.setOnClickListener((v) -> getRnrFormItemsAndPassToViaForm());
  }

  private void initRecyclerView() {
    productListRecycleView.setLayoutManager(new LinearLayoutManager(this));
    mAdapter = new AddDrugsToVIAAdapter(presenter.getInventoryViewModelList());
    productListRecycleView.setAdapter(mAdapter);
  }

  Subscriber<Void> subscriber = new Subscriber<Void>() {
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
    public void onNext(Void v) {
      mAdapter.refresh();
    }
  };

  private void getRnrFormItemsAndPassToViaForm() {
    if (validateInventory()) {
      loading();
      Subscription subscription = presenter.convertViewModelsToRnrFormItems()
          .subscribe(nextMainPageAction, errorAction);
      subscriptions.add(subscription);
    }
  }

  private boolean validateInventory() {
    int position = mAdapter.validateAll();
    if (position >= 0) {
      clearSearch();
      productListRecycleView.scrollToPosition(position);
      productListRecycleView.post(productListRecycleView::requestFocus);
      return false;
    }
    return true;
  }

  private void goToParentPage(ArrayList<RnrFormItem> addedRnrFormItemsInVIAs) {
    Intent returnIntent = new Intent();
    setResult(RESULT_OK, returnIntent);
    returnIntent.putExtra(Constants.PARAM_ADDED_DRUGS_TO_VIA, addedRnrFormItemsInVIAs);
    returnIntent.putExtra(Constants.PARAM_PERIOD_BEGIN, periodBegin);
    this.finish();
  }

  private void showErrorMessage(String msg) {
    ToastUtil.show(msg);
  }

  @Override
  public boolean onSearchStart(String query) {
    mAdapter.filter(query);
    return false;
  }

  private final Action1<ArrayList<RnrFormItem>> nextMainPageAction = rnrFormItemList -> {
    loaded();
    goToParentPage(rnrFormItemList);
  };

  private final Action1<Throwable> errorAction = throwable -> {
    loaded();
    showErrorMessage(throwable.getMessage());
  };

  public static Intent getIntentToMe(Context context, Date periodBegin, ArrayList<String> addedDrugsInVIAs) {
    Intent intent = new Intent(context, AddDrugsToVIAActivity.class);
    intent.putExtra(Constants.PARAM_PERIOD_BEGIN, periodBegin);
    intent.putExtra(Constants.PARAM_ADDED_DRUG_CODES_IN_VIA, addedDrugsInVIAs);
    return intent;
  }
}
