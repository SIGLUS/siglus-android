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

package org.openlmis.core.exceptions;


import android.util.Log;

import org.openlmis.core.BuildConfig;
import org.openlmis.core.LMISApp;

public class LMISException extends Exception {
    private static String openLMISError = "OpenLMISError";

    private String msg;

    public LMISException(String msg) {
        super(msg);
        this.msg = msg;
    }

    public LMISException(Exception e) {
        super(e);
    }

    public LMISException(Throwable throwable) {
        super(throwable);
    }

    public void reportToFabric() {
        //this will save exception messages locally
        //it only uploads to fabric server when network is available
        //so this actually behaves analogously with our sync data logic
        if (!BuildConfig.DEBUG) {
            LMISApp.getInstance().logErrorOnFabric(this);
        }
        Log.e(openLMISError, this.getMessage(), this);
    }

    public String getMsg() {
        return msg;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof LMISException && ((LMISException) o).getMsg().equals(msg);
    }

    @Override
    public int hashCode() {
        return msg != null ? msg.hashCode() : 0;
    }
}
