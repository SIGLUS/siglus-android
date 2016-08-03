package org.openlmis.core.view.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.googleAnalytics.ScreenName;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.presenter.NewStockMovementPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.InjectPresenter;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.viewmodel.StockMovementViewModel;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_stock_card_new_movement)
public class StockCardNewMovementActivity extends BaseActivity implements NewStockMovementPresenter.NewStockMovementView {

    @InjectView(R.id.ly_requested_quantity)
    View lyRequestedQuantity;

    @InjectView(R.id.tx_movement_date)
    TextView txMovementDate;

    @InjectView(R.id.ly_movement_date)
    View lyMovementDate;

    @InjectPresenter(NewStockMovementPresenter.class)
    NewStockMovementPresenter presenter;

    private String stockName;
    private String movementType;
    private Long stockCardId;

    private StockMovementItem previousMovement;

    @Override
    protected ScreenName getScreenName() {
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        stockName = getIntent().getStringExtra(Constants.PARAM_STOCK_NAME);
        movementType = getIntent().getStringExtra(Constants.PARAM_MOVEMENT_TYPE);
        stockCardId =  getIntent().getLongExtra(Constants.PARAM_STOCK_CARD_ID, 0L);

        try {
            previousMovement = presenter.loadPreviousMovement(stockCardId);
        } catch (LMISException e) {
            e.printStackTrace();
        }

        initUI();
    }

    private void initUI() {
        setTitle(movementType + " " + stockName);

        if (!movementType.equals("Issues")) {
            lyRequestedQuantity.setVisibility(View.GONE);
        }

        lyMovementDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog(presenter.getStockMovementModel(), previousMovement.getMovementDate());
            }
        });
    }

    public static Intent getIntentToMe(StockMovementsActivityNew context, String stockName, String movementType, Long stockCardId) {
        Intent intent = new Intent(context, StockCardNewMovementActivity.class);
        intent.putExtra(Constants.PARAM_STOCK_NAME, stockName);
        intent.putExtra(Constants.PARAM_MOVEMENT_TYPE, movementType);
        intent.putExtra(Constants.PARAM_STOCK_CARD_ID, stockCardId);
        return intent;
    }

    private void showDatePickerDialog(StockMovementViewModel model, Date previousMovementDate) {
        final Calendar today = GregorianCalendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(this, DatePickerDialog.BUTTON_NEUTRAL,
                new MovementDateListener(model, previousMovementDate),
                today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    class MovementDateListener implements DatePickerDialog.OnDateSetListener {

        private Date previousMovementDate;
        private StockMovementViewModel model;

        public MovementDateListener(StockMovementViewModel model, Date previousMovementDate) {
            this.previousMovementDate = previousMovementDate;
            this.model = model;
        }

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

            Date chosenDate = new GregorianCalendar(year, monthOfYear, dayOfMonth).getTime();
            if (validateStockMovementDate(previousMovementDate, chosenDate)) {
                txMovementDate.setText(DateUtil.formatDate(chosenDate));
                model.setMovementDate(DateUtil.formatDate(chosenDate));
            } else {
                ToastUtil.show(R.string.msg_invalid_stock_movement_date);
            }
        }

        private boolean validateStockMovementDate(Date previousMovementDate, Date chosenDate) {
            Calendar today = GregorianCalendar.getInstance();

            return previousMovementDate == null || !previousMovementDate.after(chosenDate) && !chosenDate.after(today.getTime());
        }
    }

}
