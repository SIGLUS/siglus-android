package org.openlmis.core.model.repository;

import android.support.annotation.NonNull;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.HealthFacilityService;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import roboguice.RoboGuice;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(LMISTestRunner.class)
public class HealthFacilityServiceRepositoryTest {

    @Test
    public void shouldGetAllExistentHealthFacilityServices() throws Exception {
        HealthFacilityServiceRepository healthFacilityServiceRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(HealthFacilityServiceRepository.class);
        ArrayList<HealthFacilityService> expectedHealthFacilityServices = new ArrayList<>();
        expectedHealthFacilityServices.add(getHealthFacilityService(1,"CPN"));
        expectedHealthFacilityServices.add(getHealthFacilityService(2,"Maternity"));
        expectedHealthFacilityServices.add(getHealthFacilityService(3,"CCR"));
        expectedHealthFacilityServices.add(getHealthFacilityService(4,"Pharmacy"));
        expectedHealthFacilityServices.add(getHealthFacilityService(5,"UATS"));
        expectedHealthFacilityServices.add(getHealthFacilityService(6,"Banco de socorro"));
        expectedHealthFacilityServices.add(getHealthFacilityService(7,"Lab"));
        expectedHealthFacilityServices.add(getHealthFacilityService(8,"Estomatologia"));

        List<HealthFacilityService> actualHealthFacilityServices = healthFacilityServiceRepository.getAll();

        assertThat(actualHealthFacilityServices, CoreMatchers.<List<HealthFacilityService>>is(expectedHealthFacilityServices));
    }

    @NonNull
    private HealthFacilityService getHealthFacilityService(int id, String name) {
        HealthFacilityService healthFacilityService = new HealthFacilityService();
        healthFacilityService.setId(id);
        healthFacilityService.setName(name);
        return healthFacilityService;
    }
}