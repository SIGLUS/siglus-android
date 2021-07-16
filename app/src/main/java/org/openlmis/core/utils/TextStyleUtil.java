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

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
import static android.text.Spanned.SPAN_POINT_MARK;

import android.text.InputFilter;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import androidx.core.content.ContextCompat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.model.Product;

public final class TextStyleUtil {

  private static final String HEADER_ARCHIVED = "[archived] ";

  private TextStyleUtil() {
  }

  public static SpannableStringBuilder getHighlightQueryKeyWord(String queryKeyWord,
      SpannableStringBuilder spannableStringBuilder) {
    if (TextUtils.isEmpty(queryKeyWord) || !spannableStringBuilder.toString().toLowerCase()
        .contains(queryKeyWord.toLowerCase())) {
      return spannableStringBuilder;
    }
    final int startIndex = spannableStringBuilder.toString().toLowerCase().indexOf(queryKeyWord.toLowerCase());
    spannableStringBuilder.setSpan(
        new ForegroundColorSpan(ContextCompat.getColor(LMISApp.getContext(), R.color.color_accent)),
        startIndex, startIndex + queryKeyWord.length(), SPAN_POINT_MARK);
    return spannableStringBuilder;
  }

  public static SpannableStringBuilder formatStyledProductName(Product product) {
    String productName = product.getFormattedProductNameWithoutStrengthAndType();
    SpannableStringBuilder styledNameBuilder = new SpannableStringBuilder(productName);
    styledNameBuilder.setSpan(new ForegroundColorSpan(
            ContextCompat.getColor(LMISApp.getContext(), R.color.color_text_secondary)),
        product.getProductNameWithoutStrengthAndType().length(), productName.length(), SPAN_POINT_MARK);
    return styledNameBuilder;
  }

  public static SpannableStringBuilder formatStyledProductNameForAddProductPage(Product product) {
    String productName = product.getFormattedProductNameWithoutStrengthAndType();
    SpannableStringBuilder styledNameBuilder = new SpannableStringBuilder(productName);
    styledNameBuilder.setSpan(new ForegroundColorSpan(
            ContextCompat.getColor(LMISApp.getContext(), R.color.color_text_secondary)),
        product.getProductNameWithoutStrengthAndType().length(), productName.length(), SPAN_POINT_MARK);
    if (product.isArchived()) {
      SpannableStringBuilder styledArchivedBuilder = new SpannableStringBuilder(HEADER_ARCHIVED);
      styledArchivedBuilder.setSpan(new ForegroundColorSpan(
              ContextCompat.getColor(LMISApp.getContext(), R.color.color_de1313)),
          0, HEADER_ARCHIVED.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
      styledArchivedBuilder.append(styledNameBuilder);
      return styledArchivedBuilder;
    }
    return styledNameBuilder;
  }


  public static SpannableStringBuilder formatStyledProductUnit(Product product) {
    String unit = product.getUnit();
    SpannableStringBuilder styledUnitBuilder = new SpannableStringBuilder(unit);
    int length = 0;
    if (product.getStrength() != null) {
      length = product.getStrength().length();
    }
    styledUnitBuilder.setSpan(new ForegroundColorSpan(
            ContextCompat.getColor(LMISApp.getContext(), R.color.color_text_secondary)),
        length, unit.length(), SPAN_POINT_MARK);
    return styledUnitBuilder;
  }

  public static InputFilter[] getSignatureLimitation() {
    InputFilter inputFilterCharacterRange = (source, start, end, dest, dstart, dend) -> {
      Pattern pattern = Pattern.compile("[a-zA-Z]");
      boolean keepOriginal = true;
      StringBuilder sb = new StringBuilder(end - start);
      for (int i = start; i < end; i++) {
        char c = source.charAt(i);
        Matcher matcher = pattern.matcher(String.valueOf(c));
        if (matcher.find()) {
          sb.append(c);
        } else {
          keepOriginal = false;
        }
      }
      if (keepOriginal) {
        return null;
      } else {
        if (source instanceof Spanned) {
          SpannableString sp = new SpannableString(sb);
          TextUtils.copySpansFrom((Spanned) source, start, sb.length(), null, sp, 0);
          return sp;
        } else {
          return sb;
        }
      }
    };
    InputFilter inputFilterMaxLength = new InputFilter.LengthFilter(
        LMISApp.getContext().getResources().getInteger(R.integer.signature_length));
    return new InputFilter[]{inputFilterCharacterRange, inputFilterMaxLength};
  }
}
