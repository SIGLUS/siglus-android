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

import android.text.InputFilter;
import android.text.Spanned;

import org.apache.commons.lang3.StringUtils;

public class InputFilterMinMax  implements InputFilter {

    int min = 0;
    int max = 0;

    public InputFilterMinMax(int min, int max){
        this.min = min;
        this.max = max;
    }

    public InputFilterMinMax(int max){
        this.max = max;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        try {
            int input = Integer.parseInt(dest.toString() + source.toString());

            if (input > max && input < min){
                return null;
            }else{
                return source;
            }
        } catch (NumberFormatException nfe) { return StringUtils.EMPTY; }
    }
}
