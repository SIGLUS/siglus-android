package org.openlmis.core.view.activity;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;

import org.openlmis.core.R;
import org.openlmis.core.googleAnalytics.TrackerActions;
import org.openlmis.core.presenter.PhysicalInventoryPresenter;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.PhysicalInventoryAdapter;
import org.openlmis.core.view.fragment.SimpleDialogFragment;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.widget.SignatureDialog;

import java.util.ArrayList;
import java.util.List;

import roboguice.inject.ContentView;
import rx.Subscriber;
import rx.Subscription;

@ContentView(R.layout.activity_inventory)
public class PhysicalInventoryActivity extends InventoryActivity {
    @InjectPresenter(PhysicalInventoryPresenter.class)
    PhysicalInventoryPresenter presenter;

    protected Subscriber<List<InventoryViewModel>> stockCardSubscriber = new Subscriber<List<InventoryViewModel>>() {
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
            mAdapter.refreshList(inventoryViewModels);
            setTotal(inventoryViewModels.size());
            loaded();
        }
    };

    @Override
    public void initUI() {
        super.initUI();
        setTitle(getResources().getString(R.string.title_physical_inventory));

        final List<InventoryViewModel> list = new ArrayList<>();
        ((ViewGroup) bottomBtn.getParent()).removeView(bottomBtn);
        View.OnClickListener saveClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.savePhysicalInventory(mAdapter.getData());
            }
        };
        View.OnClickListener completeClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.signPhysicalInventory();
                trackInventoryEvent(TrackerActions.CompleteInventory);
            }
        };
        mAdapter = new PhysicalInventoryAdapter(list, saveClickListener, completeClickListener);
        productListRecycleView.setAdapter(mAdapter);

        Subscription subscription = presenter.loadInventory().subscribe(stockCardSubscriber);
        subscriptions.add(subscription);

        btnDone.setOnClickListener(completeClickListener);
    }

    @Override
    public boolean validateInventory() {
        int position = mAdapter.physicalValidateAll();
        if (position >= 0) {
            clearSearch();
            productListRecycleView.scrollToPosition(position);
            return false;
        }
        return true;
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
                getString(R.string.msg_mmia_onback_confirm),
                getString(R.string.btn_positive),
                getString(R.string.btn_negative),
                "onBackPressed");
        dialogFragment.show(getFragmentManager(), "");
    }

    @Override
    public void goToParentPage() {
        setResult(Activity.RESULT_OK);
        finish();
    }

    public static Intent getIntentToMe(Context context) {
        return new Intent(context, PhysicalInventoryActivity.class);
    }

    @Override
    public void showSignDialog() {
        SignatureDialog signatureDialog = new SignatureDialog();
        signatureDialog.setArguments(SignatureDialog.getBundleToMe(getString(R.string.label_physical_inventory_signature_title)));
        signatureDialog.setDelegate(signatureDialogDelegate);
        signatureDialog.show(getFragmentManager());
    }

    protected SignatureDialog.DialogDelegate signatureDialogDelegate = new SignatureDialog.DialogDelegate() {
        public void onSign(String sign) {
            presenter.doInventory(mAdapter.getData(), sign);

            trackInventoryEvent(TrackerActions.ApproveInventory);
        }
    };
}
