package org.openlmis.core.model.repository;

import android.support.annotation.NonNull;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.HealthFacilityService;
import org.openlmis.core.utils.PTVUtil;
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
        ArrayList<HealthFacilityService> expectedHealthFacilityServices = PTVUtil.createDummyHealthFacilityServices();

        List<HealthFacilityService> actualHealthFacilityServices = healthFacilityServiceRepository.getAll();

        assertThat(actualHealthFacilityServices, CoreMatchers.<List<HealthFacilityService>>is(expectedHealthFacilityServices));
    }

}