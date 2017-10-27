package org.openlmis.core.builders;

import com.google.inject.AbstractModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.HealthFacilityService;
import org.openlmis.core.model.PTVProgramStockInformation;
import org.openlmis.core.model.ServiceDispensation;
import org.openlmis.core.model.repository.HealthFacilityServiceRepository;
import org.openlmis.core.utils.PTVUtil;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import roboguice.RoboGuice;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class ServiceDispensationBuilderTest {

    private HealthFacilityServiceRepository healthFacilityServiceRepository;
    ServiceDispensationBuilder serviceDispensationBuilder;

    @Before
    public void setUp() throws Exception {
        healthFacilityServiceRepository = mock(HealthFacilityServiceRepository.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new ServiceDispensationBuilderTest.MyTestModule());
        serviceDispensationBuilder = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(ServiceDispensationBuilder.class);
    }

    @Test
    public void shouldReturnServiceDispensationsForAPTVProgramStockInformation() throws Exception {
        ArrayList<HealthFacilityService> expectedHealthFacilityServices = PTVUtil.createDummyHealthFacilityServices();
        when(healthFacilityServiceRepository.getAll()).thenReturn(expectedHealthFacilityServices);
        PTVProgramStockInformation ptvProgramStockInformation = new PTVProgramStockInformation();
        ptvProgramStockInformation.setId(10L);
        List<ServiceDispensation> serviceDispensations = serviceDispensationBuilder.buildInitialServiceDispensations(ptvProgramStockInformation);

        assertThat(serviceDispensations.size(), is(expectedHealthFacilityServices.size()));
        for (ServiceDispensation serviceDispensation : serviceDispensations) {
            assertThat(serviceDispensation.getPtvProgramStockInformation(), is(ptvProgramStockInformation));
        }
    }

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(HealthFacilityServiceRepository.class).toInstance(healthFacilityServiceRepository);
        }
    }

}