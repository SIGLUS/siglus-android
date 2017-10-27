package org.openlmis.core.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.googleAnalytics.ScreenName;
import org.openlmis.core.model.PTVProgram;
import org.openlmis.core.model.PTVProgramStockInformation;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.Product;
import org.openlmis.core.presenter.PtvProgramPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.InjectPresenter;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Subscriber;

@Getter
@ContentView(R.layout.activity_ptv_report_form)
public class PTVDataReportFormActivity extends BaseActivity {

    @InjectView (R.id.tv_product_name1)
    TextView tvProduct1;

    @InjectView (R.id.tv_product_name2)
    TextView tvProduct2;

    @InjectView (R.id.tv_product_name3)
    TextView tvProduct3;

    @InjectView (R.id.tv_product_name4)
    TextView tvProduct4;

    @InjectView (R.id.tv_product_name5)
    TextView tvProduct5;

    @InjectView (R.id.tv_initial_stock1)
    TextView tvInitialStock1;

    @InjectView (R.id.tv_initial_stock2)
    TextView tvInitialStock2;

    @InjectView (R.id.tv_initial_stock3)
    TextView tvInitialStock3;

    @InjectView (R.id.tv_initial_stock4)
    TextView tvInitialStock4;

    @InjectView (R.id.tv_initial_stock5)
    TextView tvInitialStock5;

    @InjectPresenter(PtvProgramPresenter.class)
    private PtvProgramPresenter ptvProgramPresenter;

    private Period period;

    @Override
    protected ScreenName getScreenName() {
        return null;
    }

    @Override
    protected int getThemeRes() {
        return R.style.AppTheme_OrangeRed;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            initializePtvProgramPresenter();
        } catch (LMISException e) {
            e.printStackTrace();
        }
    }

    private void initializePtvProgramPresenter() throws LMISException {
        DateTime periodBegin = (DateTime) getIntent().getSerializableExtra(Constants.PARAM_PERIOD_BEGIN);
        period = new Period(periodBegin);
        ptvProgramPresenter.setPeriod(period);
        ptvProgramPresenter.buildInitialPtvProgram().subscribe(new Subscriber<PTVProgram>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(PTVProgram ptvProgram) {
                updateHeader(ptvProgram);
            }
        });
    }

    public static Intent getIntentToMe(Context context, DateTime periodBegin) {
        Intent intent = new Intent(context, PTVDataReportFormActivity.class);
        intent.putExtra(Constants.PARAM_PERIOD_BEGIN, periodBegin);
        return intent;
    }

    public void updateHeader(PTVProgram ptvProgram) {
        List<PTVProgramStockInformation> ptvProgramStocksInformation = new ArrayList<>(ptvProgram.getPtvProgramStocksInformation());
        TextView[] tvProducts = {tvProduct1, tvProduct2, tvProduct3, tvProduct4, tvProduct5};
        TextView[] tvInitialStock = {tvInitialStock1, tvInitialStock2, tvInitialStock3, tvInitialStock4, tvInitialStock5};
        for (int i = 0; i < ptvProgramStocksInformation.size(); i ++) {
            PTVProgramStockInformation ptvProgramStockInformation = ptvProgramStocksInformation.get(i);
            Product product = ptvProgramStockInformation.getProduct();
            tvProducts[i].setText(product.getPrimaryName());
            tvInitialStock[i].setText(String.valueOf(ptvProgramStockInformation.getInitialStock()));
        }
    }
}
