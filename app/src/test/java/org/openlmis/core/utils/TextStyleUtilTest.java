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

import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(LMISTestRunner.class)
public class TextStyleUtilTest {
    @Test
    public void shouldColorString() throws Exception {
        String queryKeyWord = "querykeyword";
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder(queryKeyWord);

        SpannableStringBuilder colorString = TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, stringBuilder);

        assertThat(colorString.getSpans(0, queryKeyWord.length(), ForegroundColorSpan.class)[0].getForegroundColor(),
                   is(LMISApp.getContext().getResources().getColor(R.color.color_accent)));
    }
}