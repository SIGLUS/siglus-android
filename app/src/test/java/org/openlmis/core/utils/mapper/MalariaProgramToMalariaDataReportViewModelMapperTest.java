package org.openlmis.core.utils.mapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.core.model.Implementation;
import org.openlmis.core.model.MalariaProgram;
import org.openlmis.core.view.viewmodel.malaria.ImplementationReportType;
import org.openlmis.core.view.viewmodel.malaria.ImplementationReportViewModel;
import org.openlmis.core.view.viewmodel.malaria.MalariaDataReportViewModel;

import java.util.List;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.make;
import static com.natpryce.makeiteasy.MakeItEasy.with;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openlmis.core.helpers.MalariaProgramBuilder.implementations;
import static org.openlmis.core.helpers.MalariaProgramBuilder.randomMalariaProgram;

@RunWith(MockitoJUnitRunner.class)
public class MalariaProgramToMalariaDataReportViewModelMapperTest {

    private MalariaProgram malariaProgram;
    private MalariaDataReportViewModel malariaDataReportViewModel;

    @Mock
    private ImplementationListToImplementationReportViewModelMapper implementationReportMapper;

    @InjectMocks
    private MalariaProgramToMalariaDataReportViewModelMapper mapper;
    private ImplementationReportViewModel usImplementations;
    private ImplementationReportViewModel apeImplementations;

    @Before
    public void setUp() throws Exception {
        List<Implementation> expectedImplementations = mock(List.class);
        usImplementations = mock(ImplementationReportViewModel.class);
        apeImplementations = mock(ImplementationReportViewModel.class);
        malariaProgram = make(a(randomMalariaProgram, with(implementations, expectedImplementations)));
        when(implementationReportMapper.mapUsImplementations(expectedImplementations)).thenReturn(usImplementations);
        when(implementationReportMapper.mapApeImplementations(expectedImplementations)).thenReturn(apeImplementations);

    }

    @Test
    public void shouldMapReportedDate() throws Exception {
        malariaDataReportViewModel = mapper.Map(malariaProgram);
        assertThat(malariaDataReportViewModel.getReportedDate(), is(malariaProgram.getReportedDate()));
    }

    @Test
    public void shouldMapStartPeriodDate() throws Exception {
        malariaDataReportViewModel = mapper.Map(malariaProgram);
        assertThat(malariaDataReportViewModel.getStartPeriodDate(), is(malariaProgram.getStartPeriodDate()));
    }

    @Test
    public void shouldMapEndPeriodDate() throws Exception {
        malariaDataReportViewModel = mapper.Map(malariaProgram);
        assertThat(malariaDataReportViewModel.getEndPeriodDate(), is(malariaProgram.getEndPeriodDate()));
    }

    @Test
    public void shouldMapImplementations() throws Exception {
        malariaDataReportViewModel = mapper.Map(malariaProgram);
        assertThat(malariaDataReportViewModel.getApeImplementationReportViewModel(), is(apeImplementations));
        assertThat(malariaDataReportViewModel.getUsImplementationReportViewModel(), is(usImplementations));
    }

    @Test
    public void shouldReturnDefaultViewModelWhenMalariaProgramIsNotDefined() throws Exception {
        malariaDataReportViewModel = mapper.Map(null);
        assertThat(malariaDataReportViewModel, is(notNullValue()));
        assertThat(malariaDataReportViewModel.getUsImplementationReportViewModel().getType(), is(ImplementationReportType.US));
        assertThat(malariaDataReportViewModel.getApeImplementationReportViewModel().getType(), is(ImplementationReportType.APE));
    }
}