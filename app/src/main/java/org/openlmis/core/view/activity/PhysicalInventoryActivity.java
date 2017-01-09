package org.openlmis.core.view.activity;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;

import com.viethoa.RecyclerViewFastScroller;
import com.viethoa.models.AlphabetItem;

import org.openlmis.core.R;
import org.openlmis.core.googleAnalytics.TrackerActions;
import org.openlmis.core.presenter.PhysicalInventoryPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.PhysicalInventoryAdapter;
import org.openlmis.core.view.fragment.SimpleDialogFragment;
import org.openlmis.core.view.holder.PhysicalInventoryWithLotViewHolder;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.widget.SignatureDialog;
import org.openlmis.core.view.widget.SingleClickButtonListener;

import java.util.ArrayList;
import java.util.List;

import roboguice.RoboGuice;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Subscriber;
import rx.Subscription;

@ContentView(R.layout.activity_physical_inventory)
public class PhysicalInventoryActivity extends InventoryActivity {
    @InjectView(R.id.fast_scroller)
    RecyclerViewFastScroller fastScroller;

    PhysicalInventoryPresenter presenter;

    @Override
    public void initUI() {
        super.initUI();
        bottomBtn.setVisibility(View.GONE);
        btnDone.setOnClickListener(completeClickListener);

        initPresenter();
        initRecyclerView();
        Subscription subscription = presenter.loadInventory().subscribe(getOnViewModelsLoadedSubscriber());
        subscriptions.add(subscription);
    }

    protected void initPresenter() {
        presenter = RoboGuice.getInjector(this).getInstance(PhysicalInventoryPresenter.class);
    }

    @NonNull
    @Override
    protected Subscriber<List<InventoryViewModel>> getOnViewModelsLoadedSubscriber() {
        return new Subscriber<List<InventoryViewModel>>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                ToastUtil.show(e.getMessage());
                loaded();
            }

            @Override
            public void onNext(List<InventoryViewModel> inventoryViewModels) {
                setUpFastScroller(inventoryViewModels);
                mAdapter.refresh();
                setTotal(inventoryViewModels.size());
                loaded();
            }
        };
    }

    public void setUpFastScroller(List<InventoryViewModel> viewModels) {
        List<AlphabetItem> mAlphabetItems = new ArrayList<>();
        List<String> strAlphabets = new ArrayList<>();
        for (int i = 0; i < viewModels.size(); i++) {
            String name = viewModels.get(i).getProductName();
            if (name == null || name.trim().isEmpty())
                continue;

            String word = name.substring(0, 1);
            if (!strAlphabets.contains(word)) {
                strAlphabets.add(word);
                mAlphabetItems.add(new AlphabetItem(i, word, false));
            }
        }

        fastScroller.setRecyclerView(productListRecycleView);
        fastScroller.setUpAlphabet(mAlphabetItems);
    }

    @Override
    public boolean onSearchStart(String query) {
        mAdapter.filter(query);
        setUpFastScroller(mAdapter.getFilteredList());
        return false;
    }

    @Override
    protected void initRecyclerView() {
        mAdapter = new PhysicalInventoryAdapter(presenter.getInventoryViewModelList(), getSaveOnClickListener(), completeClickListener, getRefreshCompleteCountListener());
        productListRecycleView.setAdapter(mAdapter);
    }

    private PhysicalInventoryWithLotViewHolder.InventoryItemStatusChangeListener getRefreshCompleteCountListener() {
        return new PhysicalInventoryWithLotViewHolder.InventoryItemStatusChangeListener() {
            @Override
            public void onStatusChange(boolean done) {
                setTotal(presenter.getInventoryViewModelList().size());
            }
        };
    }

    protected SingleClickButtonListener getSaveOnClickListener() {
        return new SingleClickButtonListener() {
            @Override
            public void onSingleClick(View v) {
                loading();
                Subscription subscription = presenter.saveDraftInventoryObservable().subscribe(onNextMainPageAction, errorAction);
                subscriptions.add(subscription);
            }
        };
    }

    private SingleClickButtonListener completeClickListener = new SingleClickButtonListener() {
        @Override
        public void onSingleClick(View v) {
            signPhysicalInventory();
            trackInventoryEvent(TrackerActions.CompleteInventory);
        }
    };

    @Override
    public boolean validateInventory() {
        int position = mAdapter.validateAll();
        setTotal(presenter.getInventoryViewModelList().size());
        if (position >= 0) {
            clearSearch();
            productListRecycleView.scrollToPosition(position);
            return false;
        }
        return true;
    }

    public void signPhysicalInventory() {
        if (validateInventory()) {
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
        dialogFragment.show(getFragmentManager(), "");
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
        signatureDialog.setArguments(SignatureDialog.getBundleToMe(getString(R.string.label_physical_inventory_signature_title)));
        signatureDialog.setDelegate(signatureDialogDelegate);
        signatureDialog.show(getFragmentManager());
    }

    protected SignatureDialog.DialogDelegate signatureDialogDelegate = new SignatureDialog.DialogDelegate() {
        public void onSign(String sign) {
            loading();
            Subscription subscription = presenter.doInventory(sign).subscribe(onNextMainPageAction, errorAction);
            subscriptions.add(subscription);
            trackInventoryEvent(TrackerActions.ApproveInventory);
        }
    };

    @Override
    protected void setTotal(int total) {
        tvTotal.setText(getString(R.string.label_total_complete_counts, presenter.getCompleteCount(), total));
    }
}
