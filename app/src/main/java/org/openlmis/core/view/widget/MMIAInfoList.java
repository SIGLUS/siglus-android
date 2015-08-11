package org.openlmis.core.view.widget;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
    private ArrayList<BaseInfoItem> list;
    EditText totalView = null;
    private ArrayList<EditText> editTexts = new ArrayList<>();
    private BaseInfoItem totalItem;

    public MMIAInfoList(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        setOrientation(LinearLayout.VERTICAL);
    }

    public MMIAInfoList(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MMIAInfoList(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void initView(ArrayList<BaseInfoItem> list) {
        this.list = list;
        addHeaderView();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        for (BaseInfoItem item : list) {
            View spaceLine = layoutInflater.inflate(R.layout.view_space_line, this, false);
            addView(spaceLine);
            View view = layoutInflater.inflate(R.layout.item_mmia_info, this, false);
            initView(view, item);
            addView(view);
        }
    }

    private void addHeaderView() {
        View view = LayoutInflater.from(context).inflate(R.layout.item_mmia_info, this, false);
        TextView tvName = (TextView) view.findViewById(R.id.tv_name);
        EditText etTotal = (EditText) view.findViewWithTag("tag_for_when_rotate_save_date");
        tvName.setText(R.string.list_mmia_info_header_name);
        etTotal.setText(R.string.TOTAL);
        etTotal.setEnabled(false);
        etTotal.setGravity(Gravity.CENTER);
        view.setBackgroundResource(R.color.color_mmia_info_name);
        addView(view);
    }


    private void initView(View view, final BaseInfoItem item) {
        TextView tvName = (TextView) view.findViewById(R.id.tv_name);
        EditText etValue = (EditText) view.findViewWithTag("tag_for_when_rotate_save_date");
        editTexts.add(etValue);
        tvName.setText(item.getName());

        //setId for save date when screen screen Orientation change
        etValue.setId(getId() + (int) System.currentTimeMillis());
        etValue.setText(item.getValue());

        if (isTotalValue(item)) {
            totalView = etValue;
            totalItem = item;
            totalView.setEnabled(false);
        } else {
            etValue.addTextChangedListener(new TextWatcher() {
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
            });
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
