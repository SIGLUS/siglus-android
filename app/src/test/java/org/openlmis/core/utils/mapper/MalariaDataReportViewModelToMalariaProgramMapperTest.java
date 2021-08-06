package org.openlmis.core.utils.mapper;


import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.Implementation;
import org.openlmis.core.model.MalariaProgram;
import org.openlmis.core.model.User;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.viewmodel.malaria.ImplementationReportViewModel;
import org.openlmis.core.view.viewmodel.malaria.MalariaDataReportViewModel;

import java.util.Collection;
import java.util.Date;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.make;
import static com.natpryce.makeiteasy.MakeItEasy.with;
import static org.apache.commons.lang.math.RandomUtils.nextLong;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.joda.time.DateTime.now;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.openlmis.core.helpers.MalariaDataReportBuilder.apeImplementations;
import static org.openlmis.core.helpers.MalariaDataReportBuilder.randomMalariaDataReport;
import static org.openlmis.core.helpers.MalariaDataReportBuilder.usImplementations;
import static org.openlmis.core.helpers.MalariaProgramBuilder.defaultMalariaProgram;


@RunWith(LMISTestRunner.class)
public class MalariaDataReportViewModelToMalariaProgramMapperTest {

    private MalariaDataReportViewModel malariaDataReportViewModel;

    @Mock
    private ImplementationReportViewModelToImplementationListMapper implementationReportsMapper;
    @Mock
    private ImplementationReportViewModel mockedApeImplementations;
    @Mock
    private ImplementationReportViewModel mockedUsImplementations;
    @Mock
    private Collection<Implementation> expectedImplementations;
    @InjectMocks
    private MalariaDataReportViewModelToMalariaProgramMapper mapper;
    private User user;
    private MalariaProgram malariaProgram;
    DateTimeFormatter formatter = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss z yyyy");

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        malariaProgram = make(a(defaultMalariaProgram));
        DateTimeUtils.setCurrentMillisFixed(nextLong());
        user = new User(randomAlphabetic(10), randomAlphabetic(10));
        UserInfoMgr.getInstance().setUser(user);
        malariaDataReportViewModel = make(a(randomMalariaDataReport, with(usImplementations,
                mockedUsImplementations), with(apeImplementations, mockedApeImplementations)));
        when(implementationReportsMapper.map(mockedUsImplementations, malariaProgram.getImplementations()))
                .thenReturn(expectedImplementations);
        when(implementationReportsMapper.map(mockedApeImplementations, expectedImplementations))
                .thenReturn(expectedImplementations);
    }

    @Test
    public void shouldMapUsername() throws Exception {
        mapper.map(malariaDataReportViewModel, malariaProgram);
        assertThat(malariaProgram.getUsername(), is(user.getUsername()));
    }

    @Test
    public void shouldMapReportedDate() throws Exception {
        mapper.map(malariaDataReportViewModel, malariaProgram);
        assertThat(malariaProgram.getReportedDate().toDate().toString(),
                is(new DateTime(DateUtil.getCurrentDate()).toDate().toString()));
    }

    @Test
    public void shouldMapPeriodStartDate() throws Exception {
        mapper.map(malariaDataReportViewModel, malariaProgram);
        assertThat(malariaProgram.getStartPeriodDate(), is(malariaDataReportViewModel.getStartPeriodDate()));
    }

    @Test
    public void shouldMapPeriodEndDate() throws Exception {
        mapper.map(malariaDataReportViewModel, malariaProgram);
        assertThat(malariaProgram.getEndPeriodDate(), is(malariaDataReportViewModel.getEndPeriodDate()));
    }

    @Test
    public void shouldMapImplementations() throws Exception {
        mapper.map(malariaDataReportViewModel, malariaProgram);
        assertThat(malariaProgram.getImplementations(), is(expectedImplementations));
    }

    @Test
    public void shouldMapUsernameWhenMalariaIsNotDefined() throws Exception {
        malariaProgram = null;
        malariaProgram = mapper.map(malariaDataReportViewModel, malariaProgram);
        assertThat(malariaProgram.getUsername(), is(user.getUsername()));
    }

    @Test
    public void shouldMapReportedDateWhenMalariaIsNotDefined() throws Exception {
        malariaProgram = null;
        malariaProgram = mapper.map(malariaDataReportViewModel, malariaProgram);
        assertThat(malariaProgram.getReportedDate().toDate().toString(),
                is(new DateTime(DateUtil.getCurrentDate()).toDate().toString()));
    }

    @Test
    public void shouldMapPeriodStartDateWhenMalariaIsNotDefined() throws Exception {
        malariaProgram = null;
        malariaProgram = mapper.map(malariaDataReportViewModel, malariaProgram);
        assertThat(malariaProgram.getStartPeriodDate(), is(malariaDataReportViewModel.getStartPeriodDate()));
    }

    @Test
    public void shouldMapPeriodEndDateWhenMalariaIsNotDefined() throws Exception {
        malariaProgram = null;
        malariaProgram = mapper.map(malariaDataReportViewModel, malariaProgram);
        assertThat(malariaProgram.getEndPeriodDate(), is(malariaDataReportViewModel.getEndPeriodDate()));
    }

    @Test
    public void shouldMapImplementationsWhenMalariaIsNotDefined() throws Exception {
        malariaProgram = null;
        when(implementationReportsMapper.map(mockedUsImplementations, mockedApeImplementations)).thenReturn(expectedImplementations);
        malariaProgram = mapper.map(malariaDataReportViewModel, malariaProgram);
        assertThat(malariaProgram.getImplementations(), is(expectedImplementations));
    }
}