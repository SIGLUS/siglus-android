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
package org.openlmis.core.utils;


import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;

public final class TextStyleUtil {
    private TextStyleUtil() {
    }

    public static SpannableStringBuilder getHighlightQueryKeyWord(String queryKeyWord, SpannableStringBuilder spannableStringBuilder) {
        if (queryKeyWord == null || !spannableStringBuilder.toString().toLowerCase().contains(queryKeyWord.toLowerCase())) {
            return spannableStringBuilder;
        }

        final int startIndex = spannableStringBuilder.toString().toLowerCase().indexOf(queryKeyWord.toLowerCase());

        spannableStringBuilder.setSpan(new ForegroundColorSpan(LMISApp.getContext().getResources().getColor(R.color.color_accent)),
                startIndex, startIndex + queryKeyWord.length(), Spannable.SPAN_POINT_MARK);
        return spannableStringBuilder;
    }
}
