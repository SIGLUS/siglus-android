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

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.presenter.VIARequisitionPresenter;
import org.openlmis.core.utils.SingleTextWatcher;
import org.openlmis.core.utils.ViewUtil;

import roboguice.RoboGuice;
import roboguice.inject.InjectView;

public class ViaReportConsultationNumberView extends LinearLayout {

    @InjectView(R.id.label)
    TextView tvLabel;

    @InjectView(R.id.edit_text)
    EditText editText;

    @InjectView(R.id.via_rnr_header)
    TextView viaRnrHeader;

    String labelText;
    String headerText;
    int ems;
    float width;

    private VIARequisitionPresenter presenter;

    public ViaReportConsultationNumberView(Context context) {
        super(context);
        init(context);
    }

    public ViaReportConsultationNumberView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.ViaReportConsultationNumberView, 0,
                R.style.DefaultBorderedEditText);

        labelText = attributes.getString(R.styleable.ViaReportConsultationNumberView_text);
        headerText = attributes.getString(R.styleable.ViaReportConsultationNumberView_headerText);
        ems = attributes.getInteger(R.styleable.ViaReportConsultationNumberView_ems, 4);
        width = attributes.getDimension(R.styleable.ViaReportConsultationNumberView_labelWidth, 70);

        attributes.recycle();

        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.item_requisition_report_consultation_form, this);
        RoboGuice.injectMembers(getContext(), this);
        RoboGuice.getInjector(getContext()).injectViewMembers(this);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        tvLabel.setText(labelText);
        tvLabel.setWidth((int) width);
        editText.setEms(ems);
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editText.setFilters(new InputFilter[]{new InputFilterMinMax(Integer.MAX_VALUE)});
        viaRnrHeader.setText(headerText);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        editText.setEnabled(enabled);
    }

    public void setConsultationNumbers(VIARequisitionPresenter presenter) {
        this.presenter = presenter;
        editText.setText(presenter.getConsultationNumbers());
        addTextChangedListener();
    }

    public void addTextChangedListener() {
        editText.post(new Runnable() {
            @Override
            public void run() {
                editText.addTextChangedListener(etConsultationNumbersTextWatcher);
            }
        });
    }

    TextWatcher etConsultationNumbersTextWatcher = new SingleTextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            String input = editText.getText().toString();
            if (!input.equals(presenter.getConsultationNumbers())) {
                presenter.setConsultationNumbers(input);
            }
        }
    };

    public void initUI() {
        addTextChangedListener();
        editText.setFilters(new InputFilter[]{new InputFilterMinMax(Integer.MAX_VALUE)});
    }

    public boolean validate() {
        return ViewUtil.checkEditTextEmpty(editText);
    }

    public String getValue() {
        return editText.getText().toString();
    }

    public void setEmergencyRnrHeader() {
        viaRnrHeader.setText(R.string.label_emergency_requisition_balance);
    }

    public void setEditClickListener(OnClickListener listener) {
        editText.setFocusable(false);
        editText.setOnClickListener(listener);
    }

    public void refreshNormalRnrConsultationView(VIARequisitionPresenter presenter) {
        viaRnrHeader.setText(R.string.label_requisition_header_consultation_header);
        setEnabled(true);
        setConsultationNumbers(presenter);
    }
}
