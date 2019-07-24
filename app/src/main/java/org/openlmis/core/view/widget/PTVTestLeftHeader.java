package org.openlmis.core.view.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.model.Service;
import org.openlmis.core.view.viewmodel.PTVReportViewModel;

import java.util.ArrayList;
import java.util.List;

public class PTVTestLeftHeader extends FrameLayout {
    private LayoutInflater layoutInflater;
    private PTVReportViewModel viewModel;

    public PTVTestLeftHeader(Context context) {
        super(context);
        init();
    }

    public PTVTestLeftHeader(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PTVTestLeftHeader(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PTVTestLeftHeader(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        layoutInflater = LayoutInflater.from(getContext());
    }

    private ViewGroup inflateView() {
        return (ViewGroup) layoutInflater.inflate(R.layout.fragment_ptv_left_header, this, false);
    }

    public ViewGroup initView(PTVReportViewModel viewModel) {
        this.viewModel = viewModel;
        ViewGroup inflate = inflateView();
        TextView tvName = (TextView) inflate.findViewById(R.id.tv_name);
        EditText etStock = (EditText) inflate.findViewById(R.id.et_initial_stock);
        ViewGroup service = (ViewGroup) inflate.findViewById(R.id.ll_services);
        List<EditText> services = addService(service);
        TextView tvTotal = (TextView) inflate.findViewById(R.id.tv_total);
        TextView tvReceived = (TextView) inflate.findViewById(R.id.tv_entry);
        EditText etAdjustment = (EditText) inflate.findViewById(R.id.et_adjustment);
        EditText etFinalStock = (EditText) inflate.findViewById(R.id.et_finalStock);
        setHeaderView(tvName, etStock, services, tvTotal, tvReceived, etAdjustment, etFinalStock);
        addView(inflate);
        return inflate;
    }

    private List<EditText> addService(ViewGroup service) {
        List<EditText> etServices = new ArrayList<>();
        for (Service serviceItem : viewModel.services) {
            ViewGroup inflate = (ViewGroup) layoutInflater.inflate(R.layout.item_service, this, false);
            EditText etService = (EditText) inflate.findViewById(R.id.et_service);
            etService.setId(viewModel.services.indexOf(serviceItem));
            service.addView(inflate);
            etServices.add(etService);
        }
        return etServices;
    }

    private void setHeaderView(TextView tvName,
                               EditText etStock,
                               List<EditText> services,
                               TextView tvTotal,
                               TextView tvReceived,
                               EditText etAdjustment,
                               EditText etFinalStock) {
        tvName.setText(R.string.PatientAndService);
        etStock.setText(R.string.initialStockLevel);
        tvTotal.setText(R.string.totalPtv);
        tvReceived.setText(R.string.entries);
        etAdjustment.setText(R.string.loss_and_adjustment);
        etFinalStock.setText(R.string.final_stock);
        etStock.setEnabled(false);
        etAdjustment.setEnabled(false);
        etFinalStock.setEnabled(false);
        for (EditText etService : services) {
            int serviceIndex = etService.getId();
            Service serviceCurrent = viewModel.getServices().get(serviceIndex);
            etService.setText(serviceCurrent.getName());
            etService.setEnabled(false);
        }
        setHeaderViewTextStyle(etStock, services, tvTotal, tvReceived, etAdjustment, etFinalStock);
    }

    private void setHeaderViewTextStyle(EditText etStock,
                                        List<EditText> services,
                                        TextView tvTotal,
                                        TextView tvReceived,
                                        EditText etAdjustment,
                                        EditText etFinalStock) {
        float fontSize = getResources().getDimension(R.dimen.font_size_small);
        etStock.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, fontSize);
        etStock.setTypeface(Typeface.DEFAULT_BOLD);
        tvTotal.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, fontSize);
        tvTotal.setTypeface(Typeface.DEFAULT_BOLD);
        tvReceived.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, fontSize);
        tvReceived.setTypeface(Typeface.DEFAULT_BOLD);
        etAdjustment.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, fontSize);
        etAdjustment.setTypeface(Typeface.DEFAULT_BOLD);
        etFinalStock.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, fontSize);
        etFinalStock.setTypeface(Typeface.DEFAULT_BOLD);
        for (EditText etService : services) {
            etService.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, fontSize);
            etService.setTypeface(Typeface.DEFAULT_BOLD);
        }
    }
}
