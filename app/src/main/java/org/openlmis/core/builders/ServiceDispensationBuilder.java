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
    private List<HealthFacilityService> services;

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

    public List<ServiceDispensation> buildExistentInitialServiceDispensations(PTVProgramStockInformation ptvProgramStockInformation) throws LMISException {
        List<ServiceDispensation> serviceDispensations = new ArrayList<>();
        services = getAllHealthFacilityServices();
        for (ServiceDispensation serviceDispensation : ptvProgramStockInformation.getServiceDispensations()) {
            HealthFacilityService healthFacilityService = getFacilityServiceForCurrentServiceDispensation(serviceDispensation);
            serviceDispensation.setHealthFacilityService(healthFacilityService);
            serviceDispensations.add(serviceDispensation);
        }
        return serviceDispensations;
    }

    private HealthFacilityService getFacilityServiceForCurrentServiceDispensation(ServiceDispensation serviceDispensation) {
        long id = serviceDispensation.getHealthFacilityService().getId();
        for (HealthFacilityService healthFacilityService : services) {
            if (healthFacilityService.getId() == id) {
                return healthFacilityService;
            }
        }
        return serviceDispensation.getHealthFacilityService();
    }
}
