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

import android.text.Editable;
import android.text.TextWatcher;

public class SingleTextWatcher implements TextWatcher {
    @Override
    public void beforeTextChanged(final CharSequence sequence, final int start, final int count, final int after) {
        // nothing to do
    }

    @Override
    public void afterTextChanged(final Editable editable) {
        // nothing to do
    }

    @Override
    public void onTextChanged(final CharSequence sequence, final int start, final int before, final int count) {
        // nothing to do
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        // Used to avoid add multiple Listeners in ListView
        return true;
    }
}