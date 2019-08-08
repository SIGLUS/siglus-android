package org.openlmis.core.view.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.text.Editable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.Service;
import org.openlmis.core.model.ServiceItem;
import org.openlmis.core.utils.SimpleTextWatcher;
import org.openlmis.core.view.viewmodel.PTVReportViewModel;

import java.util.ArrayList;
import java.util.List;

public class PTVTestRnrForm extends LinearLayout {
    private Context context;
    private LayoutInflater layoutInflater;
    public PTVReportViewModel viewModel;
    private ViewGroup viewGroup;
    private List<Pair<EditText, SimpleTextWatcher>> editTextConfigures = new ArrayList<>();
    private List<List<EditText>> editTextsLists = new ArrayList<>();

    public PTVTestRnrForm(Context context) {
        super(context);
        init(context);
    }

    public PTVTestRnrForm(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        View container = layoutInflater.inflate(R.layout.view_ptv_test_rnr_form, this, true);
        viewGroup = (ViewGroup) container.findViewById(R.id.ptv_from_list);
    }

    public void initView(PTVReportViewModel viewModel) {
        this.viewModel = viewModel;
        editTextsLists.clear();
        addItemView(viewModel.form.getRnrFormItemListWrapper());
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        for (Pair<EditText, SimpleTextWatcher> editTextConfigure : editTextConfigures) {
            editTextConfigure.first.removeTextChangedListener(editTextConfigure.second);
        }

    }

    private void addItemView(List<RnrFormItem> itemFormList) {
        for (RnrFormItem basicItem : itemFormList) {
            addView(basicItem);
        }
    }

    private ViewGroup inflateView() {
        return (ViewGroup) layoutInflater.inflate(R.layout.item_ptv_test_from, this, false);
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

    private ViewGroup addView(RnrFormItem item) {
        ViewGroup inflate = inflateView();
        TextView tvName = (TextView) inflate.findViewById(R.id.tv_name);
        EditText etStock = (EditText) inflate.findViewById(R.id.et_initial_stock);
        ViewGroup service = (ViewGroup) inflate.findViewById(R.id.ll_services);
        List<EditText> services = addService(service);
        TextView tvTotal = (TextView) inflate.findViewById(R.id.tv_total);
        TextView tvReceived = (TextView) inflate.findViewById(R.id.tv_entry);
        EditText etAdjustment = (EditText) inflate.findViewById(R.id.et_adjustment);
        EditText etFinalStock = (EditText) inflate.findViewById(R.id.et_finalStock);

        configureDataView(item, tvName, etStock, services, tvTotal, tvReceived, etAdjustment, etFinalStock);
        viewGroup.addView(inflate);
        return inflate;
    }

    private void configureDataView(RnrFormItem item, TextView tvName, EditText etStock, List<EditText> services, TextView tvTotal, TextView tvReceived, EditText etAdjustment, EditText etFinalStock) {
        List<EditText> editTexts = new ArrayList<>();
        tvName.setText(item.getProduct().getPrimaryName());
        etStock.setText(String.valueOf(getValue(item.getInitialAmount())));
        if (item.getIsCustomAmount()) {
            configEditText(item, etStock, tvTotal);
            editTexts.add(etStock);
        } else {
            etStock.setEnabled(false);
        }
        for (EditText etService : services) {
            int serviceIndex = etService.getId();
            Service serviceCurrent = viewModel.getServices().get(serviceIndex);
            ServiceItem serviceItem = getServiceItem(item, serviceCurrent.getCode());
            if (serviceItem != null) {
                etService.setText(getValue(serviceItem.getAmount()));
            }
            editTexts.add(etService);
            configEditText(item, etService, tvTotal);
        }
        tvTotal.setText(getValue(item.getTotalServiceQuantity()));
        tvReceived.setText(getValue(item.getReceived()));
        etAdjustment.setText(getValue(item.getAdjustment()));
        etFinalStock.setText(getValue(item.getInventory()));

        configEditText(item, etAdjustment, tvTotal);
        configEditText(item, etFinalStock, tvTotal);
        editTexts.add(etAdjustment);
        editTexts.add(etFinalStock);
        editTextsLists.add(editTexts);
    }

    private ServiceItem getServiceItem(RnrFormItem item, String code) {
        for (ServiceItem serviceItem : item.getServiceItemListWrapper()) {
            if (serviceItem.getService().getCode().equals(code)) {
                return serviceItem;
            }
        }
        return null;
    }

    private void configEditText(RnrFormItem item, EditText editText, TextView tvTotal) {
        editText.setEnabled(true);
        PTVTestRnrForm.EditTextWatcher textWatcher = new PTVTestRnrForm.EditTextWatcher(item, editText, tvTotal);
        editText.addTextChangedListener(textWatcher);
        editTextConfigures.add(new Pair<>(editText, textWatcher));
    }

    private String getValue(Long vaule) {
        return vaule == null ? "" : String.valueOf(vaule.longValue());

    }

    public boolean isCompleted() {
        for (List<EditText> editTextList : editTextsLists) {
            for (EditText editText : editTextList) {
                if (TextUtils.isEmpty(editText.getText().toString())) {
                    editText.setError(context.getString(R.string.hint_error_input));
                    return false;
                }
            }
        }
        return true;
    }

    class EditTextWatcher extends SimpleTextWatcher {
        private final RnrFormItem item;
        private final EditText editText;
        private final TextView tvTotal;

        public EditTextWatcher(RnrFormItem item, EditText editText, TextView tvTotal) {
            this.item = item;
            this.editText = editText;
            this.tvTotal = tvTotal;
        }

        @Override
        public void afterTextChanged(Editable etText) {
            switch (editText.getId()) {
                case R.id.et_initial_stock:
                    item.setInitialAmount(getEditValue(etText));
                    break;
                case R.id.et_adjustment:
                    item.setAdjustment(getEditValue(etText));
                    break;
                case R.id.et_finalStock:
                    item.setInventory(getEditValue(etText));
                    break;
                default:
                    setService(editText, etText);
            }
        }

        private void setService(EditText etService, Editable etText) {
            int serviceIndex = etService.getId();
            Service serviceCurrent = viewModel.getServices().get(serviceIndex);
            ServiceItem serviceItem = getServiceItem(item, serviceCurrent.getCode());
            if (serviceItem != null) {
                serviceItem.setAmount(getEditValue(etText));
            }

            Long total = getTotal();
            this.tvTotal.setText(getValue(total));
            item.setTotalServiceQuantity(total);
        }

        private Long getTotal() {
            long total = 0;
            Boolean haveValue = false;
            for (ServiceItem serviceItem : this.item.getServiceItemListWrapper()) {
                if (serviceItem.getAmount() != null) {
                    total += serviceItem.getAmount();
                    haveValue = true;
                }
            }
            return haveValue ? total : null;
        }

        private Long getEditValue(Editable etText) {
            Long editText;
            try {
                editText = Long.valueOf(etText.toString());
            } catch (NumberFormatException e) {
                editText = null;
            }
            return editText;
        }
    }
}
