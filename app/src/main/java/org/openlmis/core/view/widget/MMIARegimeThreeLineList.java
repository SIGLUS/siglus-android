package org.openlmis.core.view.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.model.RegimenItemThreeLines;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.presenter.MMIARequisitionPresenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MMIARegimeThreeLineList extends LinearLayout {

    private List<RegimenItemThreeLines> dataList;
    Map<String, RegimenItemThreeLines> dataMap;
    private List<EditText> patientsTotalEdits = new ArrayList<>();
    private List<EditText> patientsPharmacyEdits = new ArrayList<>();
    private MMIARequisitionPresenter presenter;
    private LayoutInflater layoutInflater;
    private TextView mmiaThreeLinePatientsTotal;
    private TextView mmiaThreeLinePharmacyTotal;

    private MMIARegimeThreeLineListener mmiaRegimeThreeLineListener;

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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MMIARegimeThreeLineList(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
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
        EditText patientsTotalEdit = viewItem.findViewById(R.id.therapetuic_total);
        EditText patientsPharmacyEdit = viewItem.findViewById(R.id.therapetuic_pharmacy);
        Long patientsAmount = itemThreeLines.getPatientsAmount();
        Long pharmacyAmount = itemThreeLines.getPharmacyAmount();

        tvNameText.setText(itemThreeLines.getRegimeTypes());
        patientsTotalEdit.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
        if (patientsAmount != null) {
            patientsTotalEdit.setText(String.valueOf(patientsAmount));
        }
        patientsTotalEdit.addTextChangedListener(new EditTextWatcher(itemThreeLines, COUNTTYPE.PATIENTSAMOUNT));
        patientsTotalEdit.setOnEditorActionListener(getOnEditorActionListener(COUNTTYPE.PATIENTSAMOUNT));
        patientsTotalEdits.add(patientsTotalEdit);
        patientsTotalEdit.setInputType(EditorInfo.TYPE_CLASS_NUMBER);

        if (pharmacyAmount != null) {
            patientsPharmacyEdit.setText(String.valueOf(pharmacyAmount));
        }
        patientsPharmacyEdit.addTextChangedListener(new EditTextWatcher(itemThreeLines, COUNTTYPE.PHARMACYAMOUNT));
        patientsPharmacyEdit.setOnEditorActionListener(getOnEditorActionListener(COUNTTYPE.PHARMACYAMOUNT));
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

    public interface MMIARegimeThreeLineListener {
        void loading();

        void loaded();
    }

    public void setRegimeThreeLineListener(MMIARegimeThreeLineListener regimeListener) {
        this.mmiaRegimeThreeLineListener = regimeListener;
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
            if (COUNTTYPE.PATIENTSAMOUNT == type) {
                item.setPatientsAmount(Long.parseLong(editable.toString()));
                mmiaThreeLinePatientsTotal.setText(String.valueOf(getTotal(type)));
            } else if (COUNTTYPE.PHARMACYAMOUNT == type) {
                item.setPharmacyAmount(Long.parseLong(editable.toString()));
                mmiaThreeLinePharmacyTotal.setText(String.valueOf(getTotal(type)));
            }
        }
    }

    public long getTotal(COUNTTYPE counttype) {
        return RnRForm.caculateTotalRegimenTypeAmount(dataList, counttype);
    }

    private TextView.OnEditorActionListener getOnEditorActionListener(COUNTTYPE counttype) {
        return new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                return false;
            }
        };
    }
}