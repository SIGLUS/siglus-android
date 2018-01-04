package org.openlmis.core.utils.mapper;

import com.google.inject.AbstractModule;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.PTVProgram;
import org.openlmis.core.model.PTVProgramStockInformation;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.ServiceDispensation;
import org.openlmis.core.model.repository.HealthFacilityServiceRepository;
import org.openlmis.core.utils.PTVUtil;
import org.openlmis.core.view.viewmodel.ptv.PTVViewModel;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import roboguice.RoboGuice;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(LMISTestRunner.class)
public class PTVProgramToPTVViewModelMapperTest {
    private HealthFacilityServiceRepository healthFacilityServiceRepository;

    private PTVProgramToPTVViewModelMapper ptvProgramToPTVViewModelMapper;

    @Before
    public void init() {
        healthFacilityServiceRepository = mock(HealthFacilityServiceRepository.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new PTVProgramToPTVViewModelMapperTest.MyTestModule());
        ptvProgramToPTVViewModelMapper = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(PTVProgramToPTVViewModelMapper.class);
    }

    @Test
    public void shouldReturnAListOfPTVViewModelsWithTheCorrectValues() throws LMISException {
        int numberOfTotalRows = 5;
        when(healthFacilityServiceRepository.getAll()).thenReturn(PTVUtil.createDummyHealthFacilityServices());
        Period period = new Period(DateTime.now());
        PTVProgram ptvProgram = PTVUtil.createDummyPTVProgram(period);
        List<PTVProgramStockInformation> ptvProgramStocksInformation = PTVUtil.createDummyPTVProgramStocksInformation();
        ptvProgram.setPtvProgramStocksInformation(ptvProgramStocksInformation);
        List<PTVViewModel> ptvViewModels = ptvProgramToPTVViewModelMapper.buildPlaceholderRows(ptvProgram);
        assertThat(ptvViewModels.size(), is(PTVUtil.createDummyHealthFacilityServices().size()+ numberOfTotalRows));
        assertQuantitiesOfFirstPTVProgramStockInformationAreTheQuantitesForTheQuantity1OfEachViewModel(ptvProgramStocksInformation, ptvViewModels);
    }

    private void assertQuantitiesOfFirstPTVProgramStockInformationAreTheQuantitesForTheQuantity1OfEachViewModel(List<PTVProgramStockInformation> ptvProgramStocksInformation, List<PTVViewModel> ptvViewModels) {
        int firstElementPosition = 0;
        List<ServiceDispensation> serviceDispensations = new ArrayList<>(ptvProgramStocksInformation.get(firstElementPosition).getServiceDispensations());
        for(int i=0; i<ptvProgramStocksInformation.size();i++){
            assertThat(ptvViewModels.get(i).getQuantity1(), is(serviceDispensations.get(i).getQuantity()));
        }
    }

    public class MyTestModule extends AbstractModule {
        @Override
        public void configure() {
            bind(HealthFacilityServiceRepository.class).toInstance(healthFacilityServiceRepository);
        }
    }
}