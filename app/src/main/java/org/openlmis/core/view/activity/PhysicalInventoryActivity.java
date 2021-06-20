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
import android.view.View;
import androidx.fragment.app.DialogFragment;
import com.viethoa.RecyclerViewFastScroller;
import org.openlmis.core.R;
import org.openlmis.core.googleanalytics.TrackerActions;
import org.openlmis.core.presenter.PhysicalInventoryPresenter;
import org.openlmis.core.view.adapter.PhysicalInventoryAdapter;
import org.openlmis.core.view.fragment.SimpleDialogFragment;
import org.openlmis.core.view.holder.PhysicalInventoryWithLotViewHolder.InventoryItemStatusChangeListener;
import org.openlmis.core.view.widget.SignatureDialog;
import org.openlmis.core.view.widget.SingleClickButtonListener;
import roboguice.RoboGuice;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Subscription;

@ContentView(R.layout.activity_physical_inventory)
public class PhysicalInventoryActivity extends InventoryActivity {

  @InjectView(R.id.fast_scroller)
  RecyclerViewFastScroller fastScroller;

  PhysicalInventoryPresenter presenter;

  public static final String KEY_FROM_PHYSICAL_COMPLETED = "Physical-Completed";

  @Override
  public void initUI() {
    super.initUI();
    bottomBtn.setVisibility(View.GONE);
    btnDone.setOnClickListener(completeClickListener);

    initPresenter();
    initRecyclerView();
    Subscription subscription = presenter.loadInventory()
        .subscribe(getOnViewModelsLoadedSubscriber());
    subscriptions.add(subscription);
  }

  protected void initPresenter() {
    presenter = RoboGuice.getInjector(this).getInstance(PhysicalInventoryPresenter.class);
  }

  @Override
  protected void initRecyclerView() {
    mAdapter = new PhysicalInventoryAdapter(presenter.getInventoryViewModelList(),
        getSaveOnClickListener(), completeClickListener, getRefreshCompleteCountListener());
    productListRecycleView.setAdapter(mAdapter);
  }

  private InventoryItemStatusChangeListener getRefreshCompleteCountListener() {
    return done -> setTotal(presenter.getInventoryViewModelList().size());
  }

  protected SingleClickButtonListener getSaveOnClickListener() {
    return new SingleClickButtonListener() {
      @Override
      public void onSingleClick(View v) {
        loading();
        Subscription subscription = presenter.saveDraftInventoryObservable()
            .subscribe(onNextMainPageAction, errorAction);
        subscriptions.add(subscription);
      }
    };
  }

  private final SingleClickButtonListener completeClickListener = new SingleClickButtonListener() {
    @Override
    public void onSingleClick(View v) {
      signPhysicalInventory();
      trackInventoryEvent(TrackerActions.COMPLETE_INVENTORY);
    }
  };

  private boolean validateInventoryFromCompleted() {
    int position = ((PhysicalInventoryAdapter) mAdapter)
        .validateAllForCompletedClick("Physical-Completed");
    setTotal(presenter.getInventoryViewModelList().size());
    if (position >= 0) {
      clearSearch();
      productListRecycleView.scrollToPosition(position);
      return false;
    }
    return true;
  }

  public void signPhysicalInventory() {
    if (validateInventoryFromCompleted()) {
      showSignDialog();
    }
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

  private boolean isDataChange() {
    return ((PhysicalInventoryAdapter) mAdapter).isHasDataChanged();
  }

  private void showDataChangeConfirmDialog() {
    DialogFragment dialogFragment = SimpleDialogFragment.newInstance(
        null,
        getString(R.string.msg_back_confirm),
        getString(R.string.btn_positive),
        getString(R.string.btn_negative),
        "onBackPressed");
    dialogFragment.show(getSupportFragmentManager(), "");
  }

  @Override
  protected void goToNextPage() {
    setResult(Activity.RESULT_OK);
    finish();
  }

  public static Intent getIntentToMe(Context context) {
    return new Intent(context, PhysicalInventoryActivity.class);
  }

  public void showSignDialog() {
    SignatureDialog signatureDialog = new SignatureDialog();
    signatureDialog.setArguments(SignatureDialog
        .getBundleToMe(getString(R.string.label_physical_inventory_signature_title)));
    signatureDialog.setDelegate(signatureDialogDelegate);
    signatureDialog.show(getSupportFragmentManager());
  }

  protected SignatureDialog.DialogDelegate signatureDialogDelegate = new SignatureDialog.DialogDelegate() {
    public void onSign(String sign) {
      loading();
      Subscription subscription = presenter.doInventory(sign).subscribe(onNextMainPageAction, errorAction);
      subscriptions.add(subscription);
      trackInventoryEvent(TrackerActions.APPROVE_INVENTORY);
    }
  };

  @Override
  protected void setTotal(int total) {
    tvTotal.setText(getString(R.string.label_total_complete_counts, presenter.getCompleteCount(), total));
  }
}
