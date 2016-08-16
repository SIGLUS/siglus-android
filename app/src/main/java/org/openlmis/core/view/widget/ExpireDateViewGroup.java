/*
 * This program is part of the OpenLMIS logistics management information
 * system platform software.
 *
 * Copyright © 2015 ThoughtWorks, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should
 * have received a copy of the GNU Affero General Public License along with
 * this program. If not, see http://www.gnu.org/licenses. For additional
 * information contact info@OpenLMIS.org
 */

package org.openlmis.core.view.widget;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;

import com.google.inject.Inject;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.fragment.SimpleDialogFragment;
import org.openlmis.core.view.viewmodel.InventoryViewModel;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import lombok.Setter;
import roboguice.RoboGuice;

public class ExpireDateViewGroup extends org.apmem.tools.layouts.FlowLayout implements View.OnClickListener {

    private Context context;
    protected List<String> expireDates;

    @Inject
    private StockRepository stockRepository;

    @Setter
    private boolean isUpdateDBImmediately;

    private InventoryViewModel model;

    private static final String FRAGMENT_TAG = "MsgDialogFragment";

    LayoutInflater inflater;
    private View tvAddExpiryDate;

    public ExpireDateViewGroup(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);

        RoboGuice.getInjector(LMISApp.getContext()).injectMembersWithoutViews(this);
        initView();
    }

    public void initExpireDateViewGroup(List<LotOnHand> lotOnHandList) {
        removeAllViews();
        for (LotOnHand lotOnHand: lotOnHandList) {
            addLotInfoView(lotOnHand);
        }
    }

    public void initExpireDateViewGroup(InventoryViewModel model, boolean isUpdateDBImmediately) {
        this.model = model;
        this.isUpdateDBImmediately = isUpdateDBImmediately;

        View addViewBtn = getChildAt(getChildCount() - 1);
        removeAllViews();
        addView(addViewBtn);

        this.expireDates = getStockCardExpireDates(model);
        if (expireDates != null) {
            for (String date : expireDates) {
                addExpireDateView(date);
            }
        }
    }

    private void initView() {
        if (!LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_lot_management)) {
            tvAddExpiryDate = inflater.inflate(R.layout.view_add_expire_date, null);
            tvAddExpiryDate.setOnClickListener(this);
            addView(tvAddExpiryDate);
        }
    }

    @Override
    public void onClick(View v) {
        showDatePicker();
    }

    private void showDatePicker() {
        final Calendar today = GregorianCalendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialogWithoutDay(context, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int lastDayOfMonth) {
                GregorianCalendar date = new GregorianCalendar(year, monthOfYear, lastDayOfMonth);
                if (today.before(date)) {
                    addDate(DateUtil.formatDateFromIntToString(year, monthOfYear, lastDayOfMonth));
                } else {
                    ToastUtil.show(R.string.msg_invalid_date);
                }
            }
        }, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));

        dialog.show();
    }

    protected void addDate(String date) {
        if (expireDates.contains(date)) {
            return;
        }
        expireDates.add(date);

        addExpireDateView(date);
        updateExpireDateToDB();
    }

    private ViewGroup addExpireDateView(final String date) {
        final ViewGroup expireDateView = (ViewGroup) inflater.inflate(R.layout.item_expire_date, null);
        TextView tvExpireDate = (TextView) expireDateView.findViewById(R.id.tx_expire_data);

        try {
            tvExpireDate.setText(DateUtil.convertDate(date, DateUtil.SIMPLE_DATE_FORMAT, DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR));
        } catch (ParseException e) {
            new LMISException(e).reportToFabric();
        }
        addView(expireDateView, getChildCount() - 1);


        View ivClear = expireDateView.findViewById(R.id.iv_clear);
        ivClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMsgDialog(expireDateView, date);
            }
        });

        return expireDateView;
    }


    private ViewGroup addLotInfoView(LotOnHand lotOnHand) {
        String lotOnHandQuantity = "" + lotOnHand.getQuantityOnHand();
        String lotInfo = lotOnHand.getLot().getLotNumber() + " - "
                + DateUtil.formatDate(lotOnHand.getLot().getExpirationDate(), DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR)
                + " - "
                + lotOnHandQuantity;

        final ViewGroup lotInfoView = (ViewGroup) inflater.inflate(R.layout.item_lot_info_for_stockcard, null);
        TextView txLotInfo = (TextView) lotInfoView.findViewById(R.id.tx_lot_info);
        txLotInfo.setText(lotInfo);
        addView(lotInfoView, getChildCount() - 1);

        lotInfoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //will add pop up action in another story
            }
        });

        return lotInfoView;
    }

    private List<String> getStockCardExpireDates(InventoryViewModel model) {
        if (model == null) {
            return new ArrayList<>();
        } else {
            return model.getExpiryDates();
        }
    }

    private void showMsgDialog(final View expireDateView, final String expireDate) {
        SimpleDialogFragment dialogFragment = SimpleDialogFragment.newInstance(
                null,
                context.getString(R.string.msg_remove_expire_date),
                context.getString(R.string.btn_ok),
                context.getString(R.string.btn_do_not_remove), null);
        dialogFragment.show(((Activity) context).getFragmentManager(), FRAGMENT_TAG);
        dialogFragment.setCallBackListener(createListener(expireDateView, expireDate));
    }

    @NonNull
    private SimpleDialogFragment.MsgDialogCallBack createListener(final View expireDateView, final String expireDate) {
        return new SimpleDialogFragment.MsgDialogCallBack() {
            @Override
            public void positiveClick(String tag) {
                removeView(expireDateView);
                expireDates.remove(expireDate);
                updateExpireDateToDB();
            }

            @Override
            public void negativeClick(String tag) {
            }
        };
    }

    private void updateExpireDateToDB() {
        model.setHasDataChanged(true);
        model.setExpiryDates(expireDates);

        if (isUpdateDBImmediately) {
            final StockCard stockCard = model.getStockCard();
            stockCard.setExpireDates(DateUtil.formatExpiryDateString(expireDates));
            stockRepository.createOrUpdate(stockCard);
        }
    }

    public void hideAddExpiryDate(boolean hideExpiryDate) {
        tvAddExpiryDate.setVisibility(hideExpiryDate ? GONE : VISIBLE);
    }
}
