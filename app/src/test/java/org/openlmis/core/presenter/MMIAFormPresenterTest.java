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

package org.openlmis.core.presenter;


import com.google.inject.AbstractModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.repository.MMIARepository;
import org.robolectric.Robolectric;

import java.sql.SQLException;

import roboguice.RoboGuice;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class MMIAFormPresenterTest {


    private MMIAFormPresenter presenter;
    private MMIARepository mmiaRepository;

    @Before
    public void setup() {
        mmiaRepository = mock(MMIARepository.class);
        RoboGuice.overrideApplicationInjector(Robolectric.application, new MyTestModule());
        MockitoAnnotations.initMocks(this);

        presenter = RoboGuice.getInjector(Robolectric.application).getInstance(MMIAFormPresenter.class);
    }

    @Test
    public void shouldGetInitMMIAForm() throws LMISException, SQLException {
        when(mmiaRepository.getDraftMMIAForm()).thenReturn(null);
        presenter.getRnrForm();
        verify(mmiaRepository).getDraftMMIAForm();
        verify(mmiaRepository).initMMIA();
    }

    @Test
    public void shouldGetDraftMMIAForm() throws LMISException {
        when(mmiaRepository.getDraftMMIAForm()).thenReturn(new RnRForm());
        presenter.getRnrForm();
        verify(mmiaRepository).getDraftMMIAForm();
        verify(mmiaRepository, never()).initMMIA();
    }

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(MMIARepository.class).toInstance(mmiaRepository);
        }
    }

}
