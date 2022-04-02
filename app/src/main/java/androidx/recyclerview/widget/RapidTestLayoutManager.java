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

package androidx.recyclerview.widget;

import android.content.Context;
import java.lang.reflect.Field;
import org.openlmis.core.exceptions.LMISException;

public class RapidTestLayoutManager extends LinearLayoutManager {

  public RapidTestLayoutManager(Context context) {
    super(context);
    try {
      Field anchorInfoField = LinearLayoutManager.class.getDeclaredField("mAnchorInfo");
      anchorInfoField.setAccessible(true);
      anchorInfoField.set(this, new MyAnchorInfo());
      mAnchorInfo.mOrientationHelper = mOrientationHelper;
    } catch (Exception ignore) {
      // In the worst case, the modification fails, and bugfix doesn't work.
      new LMISException(ignore, "rapid test align problem").reportToFabric();
    }
  }

  @Override
  public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
    //avoid editText focus changed cause recyclerView scroll
    if (mRecyclerView != null && mRecyclerView.getScrollState() != RecyclerView.SCROLL_STATE_SETTLING) {
      return super.scrollVerticallyBy(dy, recycler, state);
    }
    return 0;
  }
}
