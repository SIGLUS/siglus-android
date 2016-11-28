package org.openlmis.core.presenter;

import com.google.inject.AbstractModule;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.service.PeriodService;
import org.openlmis.core.utils.DateUtil;
import org.robolectric.RuntimeEnvironment;

import roboguice.RoboGuice;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class RapidTestReportsPresenterTest {

    private PeriodService periodService;

    @InjectMocks
    private RapidTestReportsPresenter presenter;

    @Before
    public void setUp() {

        periodService = mock(PeriodService.class);

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
            @Override
            protected void configure() {
                bind(PeriodService.class).toInstance(periodService);
            }
        });

        presenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(RapidTestReportsPresenter.class);
    }

    @Test
    public void shouldGenerateViewModelsForAllPeriods() throws Exception {
        //today period is 2016-12-21 to 2017-01-20
        LMISTestApp.getInstance().setCurrentTimeMillis(DateUtil.parseString("2016-12-31", DateUtil.DB_DATE_FORMAT).getTime());
        //first period is 2016-09-21 to 2016-10-20
        Period firstPeriod = new Period(new DateTime(DateUtil.parseString("2016-10-10", DateUtil.DB_DATE_FORMAT)));
        Period secondPeriod = new Period(new DateTime(DateUtil.parseString("2016-11-10", DateUtil.DB_DATE_FORMAT)));
        Period thirdPeriod = new Period(new DateTime(DateUtil.parseString("2016-12-10", DateUtil.DB_DATE_FORMAT)));
        Period fourthPeriod = new Period(new DateTime(DateUtil.parseString("2017-1-10", DateUtil.DB_DATE_FORMAT)));
        when(periodService.getFirstStandardPeriod()).thenReturn(firstPeriod);
        when(periodService.generateNextPeriod(firstPeriod)).thenReturn(secondPeriod);
        when(periodService.generateNextPeriod(secondPeriod)).thenReturn(thirdPeriod);
        when(periodService.generateNextPeriod(thirdPeriod)).thenReturn(fourthPeriod);
        when(periodService.generateNextPeriod(fourthPeriod)).thenReturn(null);
        presenter.generateViewModelsForAllPeriods();
        assertThat(presenter.getViewModelList().size(), is(4));
    }
}