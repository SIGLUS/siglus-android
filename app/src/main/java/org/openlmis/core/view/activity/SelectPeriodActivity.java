package org.openlmis.core.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.model.Inventory;
import org.openlmis.core.model.repository.MMIARepository;
import org.openlmis.core.model.repository.VIARepository;
import org.openlmis.core.presenter.SelectPeriodPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.view.adapter.SelectPeriodAdapter;

import java.util.List;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;


@ContentView(R.layout.activity_select_period)
public class SelectPeriodActivity extends BaseActivity implements SelectPeriodPresenter.SelectPeriodView {

    @InjectView(R.id.tv_select_period_instruction)
    protected TextView tvInstruction;

    @InjectView(R.id.vg_inventory_date_container)
    protected GridView vgContainer;

    @InjectView(R.id.btn_select_period_next)
    protected Button nextBtn;

    @InjectView(R.id.tv_select_period_warning)
    protected TextView tvSelectPeriodWarning;

    @InjectPresenter(SelectPeriodPresenter.class)
    SelectPeriodPresenter presenter;

    private SelectPeriodAdapter adapter;
    private Inventory selectedInventory;
    private String programCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.programCode = getIntent().getStringExtra(Constants.PARAM_PROGRAM_CODE);
        super.onCreate(savedInstanceState);

        init();
    }

    @Override
    protected int getThemeRes() {
        switch (programCode) {
            case MMIARepository.MMIA_PROGRAM_CODE:
                return R.style.AppTheme_AMBER;
            case VIARepository.VIA_PROGRAM_CODE:
                return R.style.AppTheme_PURPLE;
            default:
                return super.getThemeRes();
        }
    }

    private void init() {
        invalidateNextBtn();

        DateTime date = new DateTime(LMISApp.getInstance().getCurrentTimeMillis());
        tvInstruction.setText(Html.fromHtml(this.getString(R.string.label_select_close_of_period, date.monthOfYear().getAsShortText(), date.toString("dd MMM"))));

        presenter.loadData();
        adapter = new SelectPeriodAdapter();
        vgContainer.setAdapter(adapter);

        bindListeners();
    }

    private void bindListeners() {
        vgContainer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedInventory = adapter.getItem(position);
                invalidateNextBtn();
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedInventory == null) {
                    tvSelectPeriodWarning.setVisibility(View.VISIBLE);
                    return;
                }
                Intent intent = new Intent();
                intent.putExtra(Constants.PARAM_SELECTED_INVENTORY, selectedInventory);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    private void invalidateNextBtn() {
        tvSelectPeriodWarning.setVisibility(View.INVISIBLE);
    }

    public static Intent getIntentToMe(Context context, String programCode) {
        Intent intent = new Intent(context, SelectPeriodActivity.class);
        intent.putExtra(Constants.PARAM_PROGRAM_CODE, programCode);
        return intent;
    }

    @Override
    public void refreshDate(List<Inventory> inventories) {
        adapter.refreshDate(inventories);
    }
}
