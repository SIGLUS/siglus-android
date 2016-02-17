package org.openlmis.core.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.view.adapter.UnpackNumAdapter;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_select_unpack_kit_number)
public class SelectUnpackKitNumActivity extends BaseActivity {

    @InjectView(R.id.vg_unpack_num_container)
    protected GridView gridView;

    @InjectView(R.id.tv_select_unpack_kit_number)
    protected TextView tvLabel;

    @InjectView(R.id.btn_next)
    protected View btnNext;

    @InjectView(R.id.tv_select_num_warning)
    protected View tvSelectNumWarning;

    private UnpackNumAdapter adapter;

    private static final String PARAM_KIT_SOH = "param_kit_soh";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        Intent intent = getIntent();
        final String kitName = intent.getStringExtra(Constants.PARAM_KIT_NAME);
        tvLabel.setText(getString(R.string.label_select_unpack_num, kitName));

        final String productCode = intent.getStringExtra(Constants.PARAM_KIT_CODE);
        long kitSOH = intent.getLongExtra(PARAM_KIT_SOH, 0L);
        adapter = new UnpackNumAdapter(this, kitSOH, kitName);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                tvSelectNumWarning.setVisibility(View.INVISIBLE);
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gridView.getCheckedItemPosition() == GridView.INVALID_POSITION) {
                    tvSelectNumWarning.setVisibility(View.VISIBLE);
                    return;
                }

                int unpackNum = gridView.getCheckedItemPosition() + 1;
                startActivityForResult(UnpackKitActivity.getIntentToMe(SelectUnpackKitNumActivity.this, productCode, unpackNum, kitName), Constants.REQUEST_UNPACK_KIT);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.REQUEST_UNPACK_KIT) {
            setResult(Activity.RESULT_OK);
            finish();
        }
    }

    public static Intent getIntentToMe(Activity activity, String kitName, String productCode, long kitSoh) {
        Intent intent = new Intent(activity, SelectUnpackKitNumActivity.class);
        intent.putExtra(Constants.PARAM_KIT_NAME, kitName);
        intent.putExtra(Constants.PARAM_KIT_CODE, productCode);
        intent.putExtra(PARAM_KIT_SOH, kitSoh);
        return intent;
    }
}
