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

import android.app.DialogFragment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.view.widget.MMIARegimeList;
import org.robolectric.Robolectric;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class MMIAActivityTest {

    private MMIAActivity activity;
    private MMIARegimeList mmiaRegimeList;

    @Before
    public void setUp() {
        activity = Robolectric.buildActivity(MMIAActivity.class).create().get();
        activity.regimeListView = mmiaRegimeList = mock(MMIARegimeList.class);
    }

    @Test
    public void shouldShowDialogWhenDataChangedOnBackPressed() {
        when(mmiaRegimeList.hasDataChanged()).thenReturn(true);
        activity.onBackPressed();
        DialogFragment mmiaOnBackConfirmDialog = (DialogFragment) activity.getFragmentManager().findFragmentByTag("OnBackConfirmDialog");
        assertTrue(mmiaOnBackConfirmDialog.getShowsDialog());
    }

}
