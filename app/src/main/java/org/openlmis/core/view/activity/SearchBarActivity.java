package org.openlmis.core.view.activity;

import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.R;

public abstract class SearchBarActivity extends BaseActivity {

    protected SearchView searchView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search_bar, menu);

        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setQueryHint(getResources().getString(R.string.search_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return onSearchStart(newText);
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                return onSearchClosed();
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    protected void clearSearch() {
        if (searchView != null) {
            searchView.setQuery(StringUtils.EMPTY, true);
        }
    }

    public abstract boolean onSearchStart(String query);

    public abstract boolean onSearchClosed();
}
