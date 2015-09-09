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

package org.openlmis.core.view.activity;

import android.os.Bundle;

import org.openlmis.core.R;
import org.openlmis.core.component.Component;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.presenter.Presenter;

import roboguice.inject.ContentView;
import roboguice.inject.InjectFragment;

@ContentView(R.layout.activity_stockcard_list)
public class StockCardListActivity extends BaseActivity {

    @InjectFragment(R.id.stock_card_list)
    Component stockCardFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Presenter getPresenter() {
        return new Presenter() {
            @Override
            public void onStart() {

            }

            @Override
            public void onStop() {

            }

            @Override
            public void attachView(org.openlmis.core.view.View v) throws ViewNotMatchException {

            }
        };
    }

    @Override
    public boolean onSearchStart(String query) {
        stockCardFragment.onSearch(query);
        return true;
    }
}
