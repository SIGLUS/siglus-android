package org.openlmis.core.view.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.widget.Button;

import com.google.inject.Inject;

import org.openlmis.core.R;
import org.openlmis.core.presenter.InventoryPresenter;
import org.openlmis.core.presenter.Presenter;
import org.openlmis.core.view.adapter.InventoryListAdapter;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;


@ContentView(R.layout.activity_inventory)
public class InventoryActitivy extends BaseActivity {

    @InjectView(R.id.search_view)
    SearchView searchView;

    @InjectView(R.id.products_list)
    RecyclerView productListRecycleView;

    @InjectView(R.id.btn_done)
    Button btnDone;


    @Inject
    InventoryPresenter presenter;

    LinearLayoutManager mLayoutManager;
    private InventoryListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initUI();
    }

    private void initUI(){
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        productListRecycleView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new InventoryListAdapter(this, presenter);
        productListRecycleView.setAdapter(mAdapter);
    }

    @Override
    public Presenter getPresenter() {
        return presenter;
    }
}
