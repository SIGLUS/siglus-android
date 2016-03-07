package org.openlmis.core.view.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.presenter.UnpackKitPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.view.adapter.UnpackKitAdapter;
import org.openlmis.core.view.viewmodel.InventoryViewModel;

import java.util.ArrayList;
import java.util.List;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_kit_unpack)
public class UnpackKitActivity extends BaseActivity implements UnpackKitPresenter.UnpackKitView {
    @InjectView(R.id.products_list)
    protected RecyclerView productListRecycleView;

    @InjectView(R.id.btn_complete)
    protected Button completeBtn;

    @InjectView(R.id.tv_total)
    protected TextView tvTotal;

    @InjectView(R.id.tv_total_kit)
    protected TextView tvTotalKit;

    @InjectPresenter(UnpackKitPresenter.class)
    private UnpackKitPresenter presenter;

    private String kitCode;

    private UnpackKitAdapter mAdapter;
    @Override
    protected int getThemeRes() {
        return R.style.AppTheme_TEAL;
    }

    @Override
    protected void sendScreenToGoogleAnalytics() {
        LMISApp.getInstance().sendScreenToGoogleAnalytics(ScreenName.UnpackKitScreen.getScreenName(), UserInfoMgr.getInstance().getFacilityName());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        final int kitNum = intent.getIntExtra(Constants.PARAM_KIT_NUM, Integer.MIN_VALUE);
        String kitName = intent.getStringExtra(Constants.PARAM_KIT_NAME);

        tvTotalKit.setText(getString(R.string.kit_number, kitNum, kitName));

        productListRecycleView.setLayoutManager(new LinearLayoutManager(this));
        ArrayList<InventoryViewModel> list = new ArrayList<>();
        mAdapter = new UnpackKitAdapter(list);
        productListRecycleView.setAdapter(mAdapter);

        kitCode = intent.getStringExtra(Constants.PARAM_KIT_CODE);

        presenter.loadKitProducts(kitCode, kitNum);

        completeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateAll()) {
                    presenter.saveUnpackProducts(kitNum);
                }
            }
        });
    }

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
