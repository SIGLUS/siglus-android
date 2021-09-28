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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import org.openlmis.core.R;
import org.openlmis.core.googleanalytics.TrackerActions;
import org.openlmis.core.presenter.PhysicalInventoryPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.PhysicalInventoryAdapter;
import org.openlmis.core.view.fragment.SimpleDialogFragment;
import org.openlmis.core.view.widget.SignatureDialog;
import roboguice.RoboGuice;
import roboguice.inject.ContentView;
import rx.Subscription;

@ContentView(R.layout.activity_physical_inventory)
public class PhysicalInventoryActivity extends InventoryActivity<PhysicalInventoryPresenter> {

  public static final String KEY_FROM_PHYSICAL_COMPLETED = "Physical-Completed";

  public static final String KEY_FROM_PHYSICAL_VERIFY = "Physical-Verify";

  final SignatureDialog.DialogDelegate signatureDialogDelegate = new SignatureDialog.DialogDelegate() {
    public void onSign(String sign) {
      loading();
      Subscription subscription = presenter.doInventory(sign).subscribe(onNextMainPageAction, errorAction);
      subscriptions.add(subscription);
      trackInventoryEvent(TrackerActions.APPROVE_INVENTORY);
    }
  };

  public static Intent getIntentToMe(Context context) {
    return new Intent(context, PhysicalInventoryActivity.class);
  }

  @Override
  public void onBackPressed() {
    if (isSearchViewActivity()) {
      searchView.onActionViewCollapsed();
      return;
    }
    if (isDataChange()) {
      showDataChangeConfirmDialog();
      return;
    }
    super.onBackPressed();
  }

  @Override
  protected int getThemeRes() {
    return R.style.AppTheme_BLUE;
  }

  @Override
  protected boolean enableFilterProgram() {
    return false;
  }

  @Override
  protected PhysicalInventoryPresenter initPresenter() {
    return RoboGuice.getInjector(this).getInstance(PhysicalInventoryPresenter.class);
  }

  @Override
  protected void initRecyclerView() {
    mAdapter = new PhysicalInventoryAdapter(presenter.getInventoryViewModelList(), done -> setTotal());
    productListRecycleView.setLayoutManager(new LinearLayoutManager(this));
    productListRecycleView.setAdapter(mAdapter);
  }

  @Override
  protected void onSaveClick() {
    super.onSaveClick();
    loading();
    Subscription subscription = presenter.saveDraftInventoryObservable().subscribe(o -> {
      loaded();
      ToastUtil.showSystem(getString(R.string.successfully_saved));
      goToNextPage();
    }, errorAction);
    subscriptions.add(subscription);
  }

  @Override
  protected void onCompleteClick() {
    super.onCompleteClick();
    if (validateInventory()) {
      showSignDialog();
    } else {
      ToastUtil.show(getValidateFailedTips());
    }
    trackInventoryEvent(TrackerActions.COMPLETE_INVENTORY);
  }

  @Override
  protected void goToNextPage() {
    setResult(Activity.RESULT_OK);
    finish();
  }

  @Override
  protected void setTotal() {
    String formattedTotal;
    if (isInSearching()) {
      formattedTotal = getString(R.string.label_total, mAdapter.getItemCount());
    } else {
      formattedTotal = getString(R.string.label_total_complete_counts,
          presenter.getCompleteCount(),
          mAdapter.getItemCount());
    }
    tvTotal.setText(formattedTotal);
  }

  private void showDataChangeConfirmDialog() {
    DialogFragment dialogFragment = SimpleDialogFragment.newInstance(
        null,
        getString(R.string.msg_back_confirm),
        getString(R.string.btn_positive),
        getString(R.string.btn_negative),
        "onBackPressed");
    dialogFragment.show(getSupportFragmentManager(), "back_confirm_dialog");
  }

  private void showSignDialog() {
    SignatureDialog signatureDialog = new SignatureDialog();
    signatureDialog
        .setArguments(SignatureDialog.getBundleToMe(getString(R.string.label_physical_inventory_signature_title)));
    signatureDialog.setDelegate(signatureDialogDelegate);
    signatureDialog.show(getSupportFragmentManager());
  }

  private boolean isDataChange() {
    return ((PhysicalInventoryAdapter) mAdapter).isHasDataChanged();
  }
}
