package org.openlmis.core.view.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.model.RegimenItemThreeLines;
import org.openlmis.core.model.RnRForm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MMIARegimeThreeLineList extends LinearLayout {

    private List<RegimenItemThreeLines> dataList;
    Map<String, RegimenItemThreeLines> dataMap;
    private List<EditText> patientsTotalEdits = new ArrayList<>();
    private List<EditText> patientsPharmacyEdits = new ArrayList<>();
    private LayoutInflater layoutInflater;
    private TextView mmiaThreeLinePatientsTotal;
    private TextView mmiaThreeLinePharmacyTotal;

    private String ATTR_FIRST_LINE;
    private String ATTR_SECOND_LINE;
    private String ATTR_THIRD_LINE;

    public enum COUNTTYPE {
        PATIENTSAMOUNT,
        PHARMACYAMOUNT,
    }

    public MMIARegimeThreeLineList(Context context) {
        super(context);
        init();
    }

    public MMIARegimeThreeLineList(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MMIARegimeThreeLineList(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOrientation(LinearLayout.VERTICAL);
        layoutInflater = LayoutInflater.from(getContext());
        ATTR_FIRST_LINE = getString(R.string.mmia_1stline);
        ATTR_SECOND_LINE = getString(R.string.mmia_2ndline);
        ATTR_THIRD_LINE = getString(R.string.mmia_3rdline);
    }

    private String getString(int id) {
        return getContext().getString(id);
    }

    public void initView(TextView total, TextView pharmacyTotal, List<RegimenItemThreeLines> dataList) {
        mmiaThreeLinePatientsTotal = total;
        mmiaThreeLinePharmacyTotal = pharmacyTotal;
        dataMap = new HashMap<>();
        this.dataList = dataList;
        initCategoryList();
        addViewItem(dataMap.get(ATTR_FIRST_LINE));
        addViewItem(dataMap.get(ATTR_SECOND_LINE));
        addViewItem(dataMap.get(ATTR_THIRD_LINE));
        mmiaThreeLinePatientsTotal.setText(String.valueOf(getTotal(COUNTTYPE.PATIENTSAMOUNT)));
        mmiaThreeLinePharmacyTotal.setText(String.valueOf(getTotal(COUNTTYPE.PHARMACYAMOUNT)));
    }

    private void initCategoryList() {
        for (RegimenItemThreeLines itemThreeLines : dataList) {
            dataMap.put(itemThreeLines.getRegimeTypes(), itemThreeLines);
        }
    }

    private void addViewItem(RegimenItemThreeLines itemThreeLines) {
        View viewItem = layoutInflater.inflate(R.layout.fragment_mmia_requisition_regime_threeline_item, this, false);
        TextView tvNameText = viewItem.findViewById(R.id.tv_title);
        EditText patientsTotalEdit = viewItem.findViewById(R.id.therapeutic_total);
        EditText patientsPharmacyEdit = viewItem.findViewById(R.id.therapeutic_pharmacy);
        Long patientsAmount = itemThreeLines.getPatientsAmount();
        Long pharmacyAmount = itemThreeLines.getPharmacyAmount();

        tvNameText.setText(itemThreeLines.getRegimeTypes());
        if (patientsAmount != null) {
            patientsTotalEdit.setText(String.valueOf(patientsAmount));
        }
        patientsTotalEdit.addTextChangedListener(new EditTextWatcher(itemThreeLines, COUNTTYPE.PATIENTSAMOUNT));
        patientsTotalEdits.add(patientsTotalEdit);

        if (pharmacyAmount != null) {
            patientsPharmacyEdit.setText(String.valueOf(pharmacyAmount));
        }
        patientsPharmacyEdit.addTextChangedListener(new EditTextWatcher(itemThreeLines, COUNTTYPE.PHARMACYAMOUNT));
        patientsPharmacyEdits.add(patientsPharmacyEdit);

        addView(viewItem);
    }

    public boolean hasEmptyField() {
        for (RegimenItemThreeLines item : dataList) {
            if (item.getPatientsAmount() == null || item.getPharmacyAmount() == null) {
                return true;
            }
        }
        return false;
    }

    public boolean isCompleted() {
        boolean completedTotal = true;
        boolean completedPharmacy = true;
        for (EditText editText : patientsTotalEdits) {
            if (TextUtils.isEmpty(editText.getText().toString())) {
                editText.setError(getContext().getString(R.string.hint_error_input));
                editText.requestFocus();
                completedTotal = false;
            }
        }
        for (EditText editText : patientsPharmacyEdits) {
            if (TextUtils.isEmpty(editText.getText().toString())) {
                editText.setError(getContext().getString(R.string.hint_error_input));
                editText.requestFocus();
                completedPharmacy = false;
            }
        }
        return completedTotal && completedPharmacy;
    }

    public void deHighLightTotal() {
        mmiaThreeLinePatientsTotal.setBackground(getResources().getDrawable(R.color.color_page_gray));
        mmiaThreeLinePharmacyTotal.setBackground(getResources().getDrawable(R.color.color_page_gray));
    }

    public List<RegimenItemThreeLines> getDataList() {
        return dataList;
    }

    class EditTextWatcher implements TextWatcher {

        private final RegimenItemThreeLines item;
        private final COUNTTYPE type;

        public EditTextWatcher(RegimenItemThreeLines item, COUNTTYPE counttype) {
            this.item = item;
            this.type = counttype;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            Long count = 0L;
            try {
                count = Long.parseLong(editable.toString());
            } catch (NumberFormatException e) {

            }

            if (COUNTTYPE.PATIENTSAMOUNT == type) {
                item.setPatientsAmount(count);
                mmiaThreeLinePatientsTotal.setText(String.valueOf(getTotal(type)));
            } else if (COUNTTYPE.PHARMACYAMOUNT == type) {
                item.setPharmacyAmount(count);
                mmiaThreeLinePharmacyTotal.setText(String.valueOf(getTotal(type)));
            }
        }
    }

    public long getTotal(COUNTTYPE counttype) {
        return RnRForm.caculateTotalRegimenTypeAmount(dataList, counttype);
    }
}