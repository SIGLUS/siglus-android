package org.openlmis.core.builders;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.HealthFacilityService;
import org.openlmis.core.model.PTVProgramStockInformation;
import org.openlmis.core.model.ServiceDispensation;
import org.openlmis.core.model.repository.HealthFacilityServiceRepository;

import java.util.ArrayList;
import java.util.List;

public class ServiceDispensationBuilder {

    @Inject
    private HealthFacilityServiceRepository healthFacilityServiceRepository;

    @Inject
    public ServiceDispensationBuilder() {
    }

    public List<ServiceDispensation> buildInitialServiceDispensations(PTVProgramStockInformation ptvProgramStockInformation) throws LMISException {
        List<ServiceDispensation> serviceDispensations = new ArrayList<>();
        for (HealthFacilityService healthFacilityService : getAllHealthFacilityServices()) {
            ServiceDispensation serviceDispensation = new ServiceDispensation();
            serviceDispensation.setHealthFacilityService(healthFacilityService);
            serviceDispensation.setPtvProgramStockInformation(ptvProgramStockInformation);
            serviceDispensations.add(serviceDispensation);
        }

        return serviceDispensations;
    }

    private List<HealthFacilityService> getAllHealthFacilityServices() throws LMISException {
            return healthFacilityServiceRepository.getAll();
    }

}
