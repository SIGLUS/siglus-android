package org.openlmis.core.model.service;

import com.google.inject.AbstractModule;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.utils.DateUtil;
import org.robolectric.RuntimeEnvironment;

import roboguice.RoboGuice;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class PeriodServiceTest {

    private ProgramRepository mockProgramRepository;
    private RnrFormRepository mockRnrFormRepository;

    private PeriodService periodService;
    private Program programMMIA;

    @Before
    public void setup() throws LMISException {
        mockProgramRepository = mock(ProgramRepository.class);
        mockRnrFormRepository = mock(RnrFormRepository.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
        periodService = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(PeriodService.class);

        programMMIA = new Program("MMIA", "MMIA", null);
        programMMIA.setId(1l);
        when(mockProgramRepository.queryByCode(anyString())).thenReturn(programMMIA);
    }

    @Test
    public void shouldGeneratePeriodWithCurrentMonth21stAsBeginAndCurrentMonthAsEndIfNoPreviousRnrAndCurrentDateDayIsAfter25th() throws Exception {
        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_requisition_period_logic_change, true);
        LMISTestApp.getInstance().setCurrentTimeMillis(new DateTime("2016-02-28").getMillis());

        Period period = periodService.generatePeriod(programMMIA.getProgramCode(), null);
        assertThat(period.getBegin(), is(new DateTime("2016-02-21")));
        assertThat(new DateTime(period.getEnd()).getMonthOfYear(), is(3));
    }

    @Test
    public void shouldGeneratePeriodWithLastMonth21stAsBeginAndCurrentMonthAsEndIfNoPreviousRnrAndCurrentDateDayIsBefore25th() throws Exception {
        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_requisition_period_logic_change, true);
        LMISTestApp.getInstance().setCurrentTimeMillis(new DateTime("2016-02-23").getMillis());

        Period period = periodService.generatePeriod(programMMIA.getProgramCode(), null);
        assertThat(period.getBegin(), is(new DateTime("2016-01-21")));
        assertThat(new DateTime(period.getEnd()).getMonthOfYear(), is(2));
    }

    @Test
    public void shouldGeneratePeriodWithPreviousRnrEndDateAsBeginAndNextMonthAsEndDate() throws Exception {
        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_requisition_period_logic_change, true);

        RnRForm previousRnrForm = new RnRForm();
        previousRnrForm.setProgram(programMMIA);
        previousRnrForm.setPeriodEnd(DateUtil.parseString("2020-10-18", DateUtil.DB_DATE_FORMAT));
        when(mockRnrFormRepository.queryLastAuthorizedRnr(programMMIA)).thenReturn(previousRnrForm);

        Period period = periodService.generatePeriod(programMMIA.getProgramCode(), null);
        assertThat(period.getBegin().toDate(), is(previousRnrForm.getPeriodEnd()));
        assertThat(new DateTime(period.getEnd()).getMonthOfYear(), is(11));
    }


    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(ProgramRepository.class).toInstance(mockProgramRepository);
            bind(RnrFormRepository.class).toInstance(mockRnrFormRepository);
        }
    }
}