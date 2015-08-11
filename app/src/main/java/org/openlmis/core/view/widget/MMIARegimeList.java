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
import org.openlmis.core.model.Regimen;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.RnRForm;

import java.util.ArrayList;

public class MMIARegimeList extends LinearLayout {
    private Context context;
    private TextView totalView;
    private ArrayList<RegimenItem> list;
    private ArrayList<EditText> editTexts = new ArrayList<>();

    public MMIARegimeList(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        setOrientation(LinearLayout.VERTICAL);
    }

    public MMIARegimeList(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MMIARegimeList(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void initView(ArrayList<RegimenItem> regimenItems, TextView totalView) {
        this.totalView = totalView;
        this.list = regimenItems;
        addHeaderView();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        for (RegimenItem item : regimenItems) {
            View spaceLine = layoutInflater.inflate(R.layout.view_space_line, this, false);
            addView(spaceLine);
            View view = layoutInflater.inflate(R.layout.item_regime, this, false);
            initView(view, item);
            addView(view);
        }
    }

    private void addHeaderView() {
        View view = LayoutInflater.from(context).inflate(R.layout.item_regime, this, false);
        TextView tvCode = (TextView) view.findViewById(R.id.tv_code);
        TextView tvName = (TextView) view.findViewById(R.id.tv_name);
        TextView etTotal = (TextView) view.findViewWithTag("tag_for_when_rotate_save_date");
        tvName.setGravity(Gravity.CENTER);
        etTotal.setEnabled(false);
        view.setBackgroundResource(R.color.color_mmia_speed_list_header);

        tvCode.setText(R.string.list_regime_header_code);
        tvName.setText(R.string.list_regime_header_name);
        etTotal.setText(R.string.TOTAL);
        addView(view);
    }


    private void initView(View view, final RegimenItem item) {
        TextView tvCode = (TextView) view.findViewById(R.id.tv_code);
        TextView tvName = (TextView) view.findViewById(R.id.tv_name);
        EditText etTotal = (EditText) view.findViewWithTag("tag_for_when_rotate_save_date");
        //setId for save date when screen screen Orientation change
        etTotal.setId(getId() + (int) System.currentTimeMillis());

        editTexts.add(etTotal);
        Regimen regimen = item.getRegimen();
        tvCode.setText(regimen.getCode());
        tvName.setText(regimen.getName());

        if (item.getAmount() != 0) {
            etTotal.setText(String.valueOf(item.getAmount()));
        }

        if (Regimen.RegimeType.BABY.equals(regimen.getType())) {
            view.setBackgroundResource(R.color.color_regime_baby);
        } else {
            view.setBackgroundResource(R.color.color_regime_adult);
        }
        etTotal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    item.setAmount(Long.parseLong(editable.toString()));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    item.setAmount(0);
                }
                totalView.setText(String.valueOf(getTotal()));
            }
        });
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

    public long getTotal() {
        return RnRForm.getRegimenItemListAmount(list);
    }

}
