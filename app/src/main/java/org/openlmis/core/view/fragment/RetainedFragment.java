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

package org.openlmis.core.view.fragment;

import android.os.Bundle;
import java.util.HashMap;
import org.openlmis.core.presenter.Presenter;

public class RetainedFragment extends BaseFragment {

  private final HashMap map = new HashMap<>();

  // this method is only called once for this fragment
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // retain this fragment
    setRetainInstance(true);
  }

  @Override
  public Presenter initPresenter() {
    return null;
  }

  public void putData(Object key, Object data) {
    map.put(key, data);
  }

  public Object getData(Object key) {
    return map.get(key);
  }
}