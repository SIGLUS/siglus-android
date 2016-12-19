package org.openlmis.core.model.service;

import com.google.inject.AbstractModule;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.utils.DateUtil;
import org.robolectric.RuntimeEnvironment;

import roboguice.RoboGuice;

import static junit.framework.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class ProgramDataFormPeriodServiceTest {

    private StockRepository mockStockRepository;

    ProgramDataFormPeriodService periodService;

    @Before
    public void setup() throws LMISException {
        mockStockRepository = mock(StockRepository.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
        periodService = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(ProgramDataFormPeriodService.class);
    }


    @Test
    public void shouldGenerateFirstPeriodBasedOnFirstMovementDate() throws Exception {
        LMISTestApp.getInstance().setCurrentTimeMillis(DateUtil.parseString("2016-10-18", DateUtil.DB_DATE_FORMAT).getTime());

        StockMovementItem stockMovementItem = new StockMovementItem();
        stockMovementItem.setMovementDate(DateUtil.parseString("2016-10-10", DateUtil.DB_DATE_FORMAT));
        when(mockStockRepository.getFirstStockMovement()).thenReturn(stockMovementItem);

        assertThat(periodService.getFirstStandardPeriod().getBegin(), is(new DateTime(DateUtil.parseString("2016-09-21", DateUtil.DB_DATE_FORMAT))));
        assertThat(periodService.getFirstStandardPeriod().getEnd(), is(new DateTime(DateUtil.parseString("2016-10-20", DateUtil.DB_DATE_FORMAT))));
    }

    @Test
    public void shouldNotGenerateFirstPeriodIfNoMovement() throws Exception {
        when(mockStockRepository.getFirstStockMovement()).thenReturn(null);

        assertNull(periodService.getFirstStandardPeriod());
    }


    @Test
    public void shouldNotGenerateFirstPeriodIfThereIsMovementButNotPass18thYet() throws Exception {
        LMISTestApp.getInstance().setCurrentTimeMillis(DateUtil.parseString("2016-10-10", DateUtil.DB_DATE_FORMAT).getTime());

        StockMovementItem stockMovementItem = new StockMovementItem();
        stockMovementItem.setMovementDate(DateUtil.parseString("2016-10-10", DateUtil.DB_DATE_FORMAT));
        when(mockStockRepository.getFirstStockMovement()).thenReturn(stockMovementItem);

        assertNull(periodService.getFirstStandardPeriod());
    }

    @Test
    public void shouldGenerateNextPeriodShouldNotGenerateNextPeriodIfDateIsBefore18th() throws Exception {
        LMISTestApp.getInstance().setCurrentTimeMillis(DateUtil.parseString("2016-09-16", DateUtil.DB_DATE_FORMAT).getTime());

        //2016-7-21 to 2016-8-20
        Period period = Period.of(DateUtil.parseString("2016-08-12",DateUtil.DB_DATE_FORMAT));

        assertNull(periodService.generateNextPeriod(period));
    }

    @Test
    public void shouldGenerateNextPeriodShouldGenerateNextPeriodIfDateIsAfter18th() throws Exception {
        LMISTestApp.getInstance().setCurrentTimeMillis(DateUtil.parseString("2016-09-19", DateUtil.DB_DATE_FORMAT).getTime());

        //2016-7-21 to 2016-8-20
        Period period = Period.of(DateUtil.parseString("2016-08-12",DateUtil.DB_DATE_FORMAT));

        assertThat(periodService.generateNextPeriod(period).getBegin(), is(new DateTime(DateUtil.parseString("2016-08-21", DateUtil.DB_DATE_FORMAT))));
        assertThat(periodService.generateNextPeriod(period).getEnd(), is(new DateTime(DateUtil.parseString("2016-09-20", DateUtil.DB_DATE_FORMAT))));
    }


    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(StockRepository.class).toInstance(mockStockRepository);
        }
    }
}