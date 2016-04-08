package org.openlmis.core.view.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.googleAnalytics.ScreenName;
import org.openlmis.core.presenter.UnpackKitPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.view.adapter.UnpackKitAdapter;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.widget.SignatureDialog;

import java.util.ArrayList;
import java.util.List;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_kit_unpack)
public class UnpackKitActivity extends BaseActivity implements UnpackKitPresenter.UnpackKitView {
    @InjectView(R.id.products_list)
    protected RecyclerView productListRecycleView;

    @InjectView(R.id.tv_total)
    protected TextView tvTotal;

    @InjectView(R.id.tv_total_kit)
    protected TextView tvTotalKit;

    @InjectPresenter(UnpackKitPresenter.class)
    private UnpackKitPresenter presenter;

    private String kitCode;

    protected UnpackKitAdapter mAdapter;
    private int kitNum;

    @Override
    protected int getThemeRes() {
        return R.style.AppTheme_TEAL;
    }

    @Override
    protected ScreenName getScreenName() {
        return ScreenName.UnpackKitScreen;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        kitNum = intent.getIntExtra(Constants.PARAM_KIT_NUM, 0);
        String kitName = intent.getStringExtra(Constants.PARAM_KIT_NAME);

        tvTotalKit.setText(getString(R.string.kit_number, kitNum, kitName));

        productListRecycleView.setLayoutManager(new LinearLayoutManager(this));
        ArrayList<InventoryViewModel> list = new ArrayList<>();
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateAll()) {
                    if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_signature_for_unpack_kit)) {
                        showSignDialog();
                    } else {
                        presenter.saveUnpackProducts(kitNum, "");
                    }
                }
            }
        };
        mAdapter = new UnpackKitAdapter(list, onClickListener);
        productListRecycleView.setAdapter(mAdapter);

        kitCode = intent.getStringExtra(Constants.PARAM_KIT_CODE);

        presenter.loadKitProducts(kitCode, kitNum);

    }

    private void showSignDialog() {
        SignatureDialog signatureDialog = new SignatureDialog();
        signatureDialog.setArguments(SignatureDialog.getBundleToMe(getString(R.string.dialog_unpack_kit_signature)));
        signatureDialog.setDelegate(signatureDialogDelegate);
        signatureDialog.show(getFragmentManager(), "signature_dialog_for_unpack_kit");
    }

    protected SignatureDialog.DialogDelegate signatureDialogDelegate = new SignatureDialog.DialogDelegate() {
        @Override
        public void onCancel() {
        }

        @Override
        public void onSign(String sign) {
            presenter.saveUnpackProducts(kitNum, sign);
        }
    };

    private void setTotal(int total) {
        tvTotal.setText(getString(R.string.label_total, total));
    }

    public static Intent getIntentToMe(Context context, String code, int num, String kitName) {
        Intent intent = new Intent(context, UnpackKitActivity.class);
        intent.putExtra(Constants.PARAM_KIT_CODE, code);
        intent.putExtra(Constants.PARAM_KIT_NUM, num);
        intent.putExtra(Constants.PARAM_KIT_NAME, kitName);
        return intent;
    }

    @Override
    public void refreshList(List<InventoryViewModel> inventoryViewModels) {
        mAdapter.refreshList(inventoryViewModels);
        setTotal(inventoryViewModels.size());
    }

    @Override
    public void saveSuccess() {
        setResult(Activity.RESULT_OK);
        finish();
    }

    public boolean validateAll() {
        int position = mAdapter.validateAll();
        if (position >= 0) {
            productListRecycleView.scrollToPosition(position);
            return false;
        }
        return true;
    }

}
