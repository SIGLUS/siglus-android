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

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import org.openlmis.core.LMISApp;

public final class DisplayUtil {
    private DisplayUtil(){

    }

    public static int getScreenWidth() {
        Point size = new Point();
        ((WindowManager) LMISApp.getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getSize(size);
        return size.x;
    }

    public static int getStatusBarHeight() {
        int result = 0;
        int resourceId = LMISApp.getContext().getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = LMISApp.getContext().getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
