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
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.openlmis.core.R;

public class BorderedEditText extends LinearLayout{

    LayoutInflater layoutInflater;
    Context context;
    String text;
    int ems;
    float width;

    TextView label;
    EditText editText;

    public BorderedEditText(Context context, AttributeSet attrs){
        super(context, attrs);
        this.context = context;

        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.BorderedEditText, 0,
                R.style.DefaultBorderedEditText);

        text = attributes.getString(R.styleable.BorderedEditText_text);
        ems = attributes.getInteger(R.styleable.BorderedEditText_ems, 4);
        width = attributes.getDimension(R.styleable.BorderedEditText_labelWidth, 40);

        initUI();
    }

    private void initUI(){
        layoutInflater = LayoutInflater.from(context);
        View contentView = layoutInflater.inflate(R.layout.view_bordered_edittext, this, true);

        label = (TextView)contentView.findViewById(R.id.label);
        editText = (EditText) contentView.findViewById(R.id.edit_text);

        label.setText(text);
        label.setWidth((int) width);
        editText.setEms(ems);
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
    }
}
