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

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import androidx.appcompat.widget.SearchView;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.R;
import org.openlmis.core.utils.DisplayUtil;

@SuppressWarnings("squid:S110")
public abstract class SearchBarActivity extends BaseActivity {

  protected SearchView searchView;
  private ImageView closeButton;

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu_search_bar, menu);

    searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
    searchView.setQueryHint(getResources().getString(R.string.search_hint));
    searchView.setMaxWidth(DisplayUtil.getScreenWidth());
    closeButton = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
    closeButton.setImageResource(R.drawable.icon_search_view_close_white);
    closeButton.setAlpha(0.7F);
    changeSearchButtonUI();

    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String query) {
        return false;
      }

      @Override
      public boolean onQueryTextChange(String newText) {
        closeButton.setAlpha(TextUtils.isEmpty(newText) ? 0.7F : 1F);
        return onSearchStart(newText);
      }
    });

    searchView.setOnCloseListener(() -> true);

    return super.onCreateOptionsMenu(menu);
  }

  private void changeSearchButtonUI() {
    final View searchButton = searchView.findViewById(R.id.search_button);

    LayoutParams params = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
    params.width = (int) getResources().getDimension(R.dimen.search_button_width);
    params.setMargins(0, 0, (int) getResources().getDimension(R.dimen.search_button_right_margin), 0);
    searchButton.setLayoutParams(params);
  }

  protected void clearSearch() {
    if (searchView != null) {
      searchView.setQuery(StringUtils.EMPTY, true);
    }
  }

  @Override
  public void onBackPressed() {
    if (isSearchViewActivity()) {
      searchView.onActionViewCollapsed();
    } else {
      super.onBackPressed();
    }
  }

  protected boolean isSearchViewActivity() {
    return !searchView.isIconified();
  }

  public abstract boolean onSearchStart(String query);

}
