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
import android.text.InputFilter;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.openlmis.core.R;

public class ViaReportConsultationFormView extends LinearLayout {

    String labelText;
    String headerText;
    int ems;
    float width;

    TextView tvLabel;
    EditText editText;

    public ViaReportConsultationFormView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.ViaReportConsultationFormView, 0,
                R.style.DefaultBorderedEditText);

        labelText = attributes.getString(R.styleable.ViaReportConsultationFormView_text);
        headerText = attributes.getString(R.styleable.ViaReportConsultationFormView_headerText);
        ems = attributes.getInteger(R.styleable.ViaReportConsultationFormView_ems, 4);
        width = attributes.getDimension(R.styleable.ViaReportConsultationFormView_labelWidth, 70);

        attributes.recycle();

        inflateLayout();
    }

    private void inflateLayout() {
        inflate(getContext(), R.layout.item_requisition_report_consultation_form, this);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        tvLabel = (TextView) findViewById(R.id.label);
        tvLabel.setText(labelText);
        tvLabel.setWidth((int) width);

        editText = (EditText) findViewById(R.id.edit_text);
        editText.setEms(ems);
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editText.setFilters(new InputFilter[]{new InputFilterMinMax(Integer.MAX_VALUE)});

        ((TextView) findViewById(R.id.header)).setText(headerText);
    }
}
