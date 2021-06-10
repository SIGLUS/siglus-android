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
import android.text.TextWatcher;
import android.util.AttributeSet;
import java.util.ArrayList;

public class CleanableEditText extends androidx.appcompat.widget.AppCompatEditText {

  private ArrayList<TextWatcher> mListeners = null;

  public CleanableEditText(Context ctx) {
    super(ctx);
  }

  public CleanableEditText(Context ctx, AttributeSet attrs) {
    super(ctx, attrs);
  }

  public CleanableEditText(Context ctx, AttributeSet attrs, int defStyle) {
    super(ctx, attrs, defStyle);
  }

  @Override
  public void addTextChangedListener(TextWatcher watcher) {
    if (mListeners == null) {
      mListeners = new ArrayList<>();
    }
    mListeners.add(watcher);
    super.addTextChangedListener(watcher);
  }

  @Override
  public void removeTextChangedListener(TextWatcher watcher) {
    if (mListeners != null) {
      int i = mListeners.indexOf(watcher);
      if (i >= 0) {
        mListeners.remove(i);
      }
    }
    super.removeTextChangedListener(watcher);
  }

  public void clearTextChangedListeners() {
    if (mListeners != null) {
      for (TextWatcher watcher : mListeners) {
        super.removeTextChangedListener(watcher);
      }
      mListeners.clear();
      mListeners = null;
    }
  }
}
