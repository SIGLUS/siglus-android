package org.openlmis.core.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;

import org.openlmis.core.R;
import org.openlmis.core.presenter.UnpackKitPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.view.adapter.UnpackKitAdapter;
import org.openlmis.core.view.viewmodel.StockCardViewModel;

import java.util.ArrayList;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_kit_unpack)
public class UnpackKitActivity extends BaseActivity {

    @InjectView(R.id.products_list)
    protected RecyclerView productListRecycleView;

    @InjectView(R.id.btn_complete)
    protected Button completeBtn;

    @InjectPresenter(UnpackKitPresenter.class)
    private UnpackKitPresenter presenter;

    private String kitCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        productListRecycleView.setLayoutManager(new LinearLayoutManager(this));
        ArrayList<StockCardViewModel> list = new ArrayList<>();
        UnpackKitAdapter mAdapter = new UnpackKitAdapter(list);
        productListRecycleView.setAdapter(mAdapter);

        kitCode = getIntent().getStringExtra(Constants.PARAM_KIT_CODE);

        presenter.loadKitProducts(kitCode);
    }

    public static Intent getIntentToMe(Context context, String code) {
        Intent intent = new Intent(context, UnpackKitActivity.class);
        intent.putExtra(Constants.PARAM_KIT_CODE, code);
        return intent;
    }
}
