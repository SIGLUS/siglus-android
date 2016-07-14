package org.openlmis.core.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.openlmis.core.R;
import org.openlmis.core.googleAnalytics.ScreenName;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.AddDrugsToFormAdapter;
import org.openlmis.core.view.viewmodel.InventoryViewModel;

import java.util.ArrayList;
import java.util.List;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Subscriber;

@ContentView(R.layout.activity_add_drugs_to_form)
public class AddDrugsToFormActivity extends SearchBarActivity {

    @InjectView(R.id.btn_complete)
    public View btnComplete;

    @InjectView(R.id.products_list)
    public RecyclerView productListRecycleView;

//    @InjectPresenter(ProductPresenter.class)
//    ProductPresenter presenter;

    protected AddDrugsToFormAdapter mAdapter;

    @Override
    protected ScreenName getScreenName() {
        return ScreenName.AddDrugsToFormScreen;
    }

    @Override
    protected int getThemeRes() {
        return R.style.AppTheme_PURPLE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        productListRecycleView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new AddDrugsToFormAdapter(new ArrayList<InventoryViewModel>());
        productListRecycleView.setAdapter(mAdapter);
        loading();
//        Subscription subscription = presenter.loadEmergencyProducts().subscribe(subscriber);
//        subscriptions.add(subscription);

        btnComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    Subscriber<List<InventoryViewModel>> subscriber = new Subscriber<List<InventoryViewModel>>() {
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
        public void onNext(List<InventoryViewModel> data) {
            mAdapter.refreshList(data);
        }
    };

    public static Intent getIntentToMe(Context context) {
        Intent intent = new Intent(context, AddDrugsToFormActivity.class);
        return intent;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    @Override
    public boolean onSearchStart(String query) {
        mAdapter.filter(query);
        return false;
    }
}
