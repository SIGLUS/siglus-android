package org.openlmis.core.view.widget;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.model.BaseInfoItem;
import org.openlmis.core.model.repository.MIMIARepository;

import java.util.ArrayList;

public class MMIAInfoList extends LinearLayout {
    private Context context;
    EditText totalView = null;
    private ArrayList<EditText> editTexts = new ArrayList<>();
    private BaseInfoItem totalItem;
    private LayoutInflater layoutInflater;
    private ArrayList<BaseInfoItem> list;

    public MMIAInfoList(Context context) {
        super(context);
        init(context);
    }

    public MMIAInfoList(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        setOrientation(LinearLayout.VERTICAL);
        layoutInflater = LayoutInflater.from(context);
    }

    public void initView(ArrayList<BaseInfoItem> list) {
        this.list = list;
        addHeaderView();

        for (BaseInfoItem item : list) {
            if (item != null) {
                addItemView(item);
            }
        }
    }

    private void addItemView(BaseInfoItem item) {
        addItemView(item, false);
    }

    private void addDivideView() {
        addView(layoutInflater.inflate(R.layout.view_space_line, this, false));
    }

    private void addHeaderView() {
        addItemView(null, true);
    }


    private void addItemView(BaseInfoItem item, boolean isHeaderView) {
        View view = layoutInflater.inflate(R.layout.item_mmia_info, this, false);
        TextView tvName = (TextView) view.findViewById(R.id.tv_name);
        EditText etValue = (EditText) view.findViewWithTag("tag_for_when_rotate_save_date");

        if (isHeaderView) {
            tvName.setText(R.string.list_mmia_info_header_name);
            etValue.setText(R.string.TOTAL);
            etValue.setEnabled(false);
            etValue.setGravity(Gravity.CENTER);
            view.setBackgroundResource(R.color.color_mmia_info_name);
        } else {
            tvName.setText(item.getName());
            editTexts.add(etValue);
            etValue.setText(item.getValue());

            if (isTotalValue(item)) {
                totalView = etValue;
                totalItem = item;
                totalView.setEnabled(false);
            } else {
                etValue.addTextChangedListener(new EditTextWatcher(item));
            }
        }
        addView(view);
        addDivideView();
    }

    class EditTextWatcher implements android.text.TextWatcher {

        private final BaseInfoItem item;

        public EditTextWatcher(BaseInfoItem item) {
            this.item = item;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            item.setValue(editable.toString());
            if (totalView != null) {
                String total = String.valueOf(getTotal());
                totalItem.setValue(total);
                totalView.setText(total);
            }
        }
    }

    public long getTotal() {
        long totalRegimenNumber = 0;
        for (BaseInfoItem item : list) {
            if (isTotalValue(item)) {
                continue;
            }
            try {
                totalRegimenNumber += Long.parseLong(item.getValue());
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return totalRegimenNumber;
    }

    private boolean isTotalValue(BaseInfoItem item) {
        return MIMIARepository.ATTR_TOTAL_PATIENTS.equals(item.getName());
    }


    public boolean complete() {
        for (EditText editText : editTexts) {
            if (TextUtils.isEmpty(editText.getText().toString())) {
                editText.setError(context.getString(R.string.error_input));
                editText.requestFocus();
                return false;
            }
        }
        return true;
    }

}
