package org.openlmis.core.model.repository;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.HealthFacilityService;
import org.openlmis.core.model.PTVProgram;
import org.openlmis.core.model.PTVProgramStockInformation;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.ServiceDispensation;
import org.openlmis.core.utils.PTVUtil;
import org.robolectric.RuntimeEnvironment;

import roboguice.RoboGuice;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;


@RunWith(LMISTestRunner.class)
public class ServiceDispensationRepositoryTest {

    private ServiceDispensationRepository serviceDispensationRepository;

    @Before
    public void setUp() throws Exception {
        serviceDispensationRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(ServiceDispensationRepository.class);
    }

    @Test
    public void shouldSaveServiceDispensationsWhenPtvProgramStockInformationExists() throws Exception {
        Period period = new Period(DateTime.now());
        PTVProgram ptvProgram = PTVUtil.createDummyPTVProgram(period);
        Product product = Product.dummyProduct();
        PTVProgramStockInformation ptvProgramStockInformation = new PTVProgramStockInformation();
        ptvProgramStockInformation.setProduct(product);
        ptvProgramStockInformation.setPtvProgram(ptvProgram);
        HealthFacilityService healthFacilityService = new HealthFacilityService();
        ServiceDispensation serviceDispensation = new ServiceDispensation();
        serviceDispensation.setPtvProgramStockInformation(ptvProgramStockInformation);
        serviceDispensation.setHealthFacilityService(healthFacilityService);
        ServiceDispensation serviceDispensation2 = new ServiceDispensation();
        serviceDispensation2.setPtvProgramStockInformation(ptvProgramStockInformation);
        serviceDispensation2.setHealthFacilityService(healthFacilityService);

        boolean isSaved = serviceDispensationRepository.save(newArrayList(serviceDispensation,serviceDispensation2));

        assertThat(isSaved, is(true));
    }

    @Test (expected = LMISException.class)
    public void shouldThrowExceptionTryingToSaveServiceDispensationsWhenForeignObjectsAreNull() throws Exception {
        ServiceDispensation serviceDispensation = new ServiceDispensation();
        ServiceDispensation serviceDispensation2 = new ServiceDispensation();

        serviceDispensationRepository.save(newArrayList(serviceDispensation, serviceDispensation2));
    }
}