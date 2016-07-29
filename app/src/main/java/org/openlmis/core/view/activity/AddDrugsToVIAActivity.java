package org.openlmis.core.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.openlmis.core.R;
import org.openlmis.core.googleAnalytics.ScreenName;
import org.openlmis.core.model.AddedDrugInVIA;
import org.openlmis.core.presenter.AddDrugsToVIAPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.AddDrugsToVIAAdapter;
import org.openlmis.core.view.viewmodel.InventoryViewModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Subscriber;
import rx.Subscription;

@ContentView(R.layout.activity_add_drugs_to_via)
public class AddDrugsToVIAActivity extends SearchBarActivity implements AddDrugsToVIAPresenter.AddDrugsToVIAView{

    @InjectView(R.id.btn_complete)
    public View btnComplete;

    @InjectView(R.id.products_list)
    public RecyclerView productListRecycleView;

    @InjectPresenter(AddDrugsToVIAPresenter.class)
    AddDrugsToVIAPresenter presenter;

    protected AddDrugsToVIAAdapter mAdapter;
    private Date periodBegin;

    private List<String> existingAdditionalProductList;

    @Override
    protected ScreenName getScreenName() {
        return ScreenName.AddDrugsToVIAScreen;
    }

    @Override
    protected int getThemeRes() {
        return R.style.AppTheme_PURPLE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        periodBegin = ((Date) getIntent().getSerializableExtra(Constants.PARAM_PERIOD_BEGIN));
        existingAdditionalProductList = (List<String>) getIntent().getSerializableExtra(Constants.PARAM_ADDED_DRUG_CODES_IN_VIA);

        productListRecycleView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new AddDrugsToVIAAdapter(new ArrayList<InventoryViewModel>());
        productListRecycleView.setAdapter(mAdapter);
        loading();
        Subscription subscription = presenter.loadActiveProductsNotInVIAForm(existingAdditionalProductList).subscribe(subscriber);
        subscriptions.add(subscription);

        btnComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.convertViewModelsToDataAndPassToParentScreen(mAdapter.getCheckedProducts());
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

    @Override
    public boolean validateInventory() {
        int position = mAdapter.validateAll();
        if (position >= 0) {
            clearSearch();

            productListRecycleView.scrollToPosition(position);
            return false;
        }
        return true;
    }

    @Override
    public void goToParentPage(ArrayList<AddedDrugInVIA> addedDrugsInVIAs) {
        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);
        returnIntent.putParcelableArrayListExtra(Constants.PARAM_ADDED_DRUGS_TO_VIA, addedDrugsInVIAs);
        returnIntent.putExtra(Constants.PARAM_PERIOD_BEGIN, periodBegin);
        this.finish();
    }

    @Override
    public void showErrorMessage(String msg) {
        ToastUtil.show(msg);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    @Override
    public boolean onSearchStart(String query) {
        mAdapter.filter(query);
        return false;
    }

    public static Intent getIntentToMe(Context context, Date periodBegin, Date periodEnd, ArrayList<String> addedDrugsInVIAs) {
        Intent intent = new Intent(context, AddDrugsToVIAActivity.class);
        intent.putExtra(Constants.PARAM_PERIOD_BEGIN, periodBegin);
        intent.putExtra(Constants.PARAM_PERIOD_END, periodEnd);
        intent.putExtra(Constants.PARAM_ADDED_DRUG_CODES_IN_VIA, addedDrugsInVIAs);
        return intent;
    }
}
