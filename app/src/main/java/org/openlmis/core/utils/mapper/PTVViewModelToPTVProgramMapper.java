package org.openlmis.core.utils.mapper;

import com.google.inject.Inject;

import org.openlmis.core.model.PTVProgram;
import org.openlmis.core.model.PTVProgramStockInformation;
import org.openlmis.core.model.ServiceDispensation;
import org.openlmis.core.view.viewmodel.ptv.PTVViewModel;

import java.util.List;

import static org.openlmis.core.utils.Constants.LOSSES_AND_ADJUSTMENTS;
import static org.openlmis.core.utils.Constants.PTV_PRODUCT_FIFTH_CODE;
import static org.openlmis.core.utils.Constants.PTV_PRODUCT_FIRST_CODE;
import static org.openlmis.core.utils.Constants.PTV_PRODUCT_FOURTH_CODE;
import static org.openlmis.core.utils.Constants.PTV_PRODUCT_SECOND_CODE;
import static org.openlmis.core.utils.Constants.PTV_PRODUCT_THIRD_CODE;
import static org.openlmis.core.utils.Constants.REQUISITIONS;

public class PTVViewModelToPTVProgramMapper {

    @Inject
    public PTVViewModelToPTVProgramMapper() {
    }

    public PTVProgram convertToPTVProgram(List<PTVViewModel> viewModels, PTVProgram ptvProgram) {

        for (PTVProgramStockInformation ptvProgramStockInformation : ptvProgram.getPtvProgramStocksInformation()) {
            for (ServiceDispensation service : ptvProgramStockInformation.getServiceDispensations()) {
                putViewModelsAmountsInPTVFields(ptvProgramStockInformation, service, viewModels);
            }
        }
        return ptvProgram;
    }

    private void putViewModelsAmountsInPTVFields(PTVProgramStockInformation ptvProgramStockInformation, ServiceDispensation service, List<PTVViewModel> viewModels) {
        for (PTVViewModel ptvViewModel : viewModels) {
            switch (ptvProgramStockInformation.getProduct().getCode()) {
                case PTV_PRODUCT_FIRST_CODE:
                    setQuantitiesForEachProduct(ptvProgramStockInformation, service, ptvViewModel, ptvViewModel.getQuantity1());
                    break;
                case PTV_PRODUCT_SECOND_CODE:
                    setQuantitiesForEachProduct(ptvProgramStockInformation, service, ptvViewModel, ptvViewModel.getQuantity2());
                    break;
                case PTV_PRODUCT_THIRD_CODE:
                    setQuantitiesForEachProduct(ptvProgramStockInformation, service, ptvViewModel, ptvViewModel.getQuantity3());
                    break;
                case PTV_PRODUCT_FOURTH_CODE:
                    setQuantitiesForEachProduct(ptvProgramStockInformation, service, ptvViewModel, ptvViewModel.getQuantity4());
                    break;
                case PTV_PRODUCT_FIFTH_CODE:
                    setQuantitiesForEachProduct(ptvProgramStockInformation, service, ptvViewModel, ptvViewModel.getQuantity5());
                    break;
            }
        }
    }

    private void setQuantitiesForEachProduct(PTVProgramStockInformation ptvProgramStockInformation, ServiceDispensation service, PTVViewModel ptvViewModel, long quantity) {
        String serviceName = service.getHealthFacilityService().getName();
        if (serviceName.equals(ptvViewModel.getPlaceholderItemName())) {
            service.setQuantity(quantity);
        }
        if (ptvViewModel.getPlaceholderItemName().equals(REQUISITIONS)) {
            ptvProgramStockInformation.setRequisition(quantity);
        }
        if (ptvViewModel.getPlaceholderItemName().equals(LOSSES_AND_ADJUSTMENTS)) {
            ptvProgramStockInformation.setLossesAndAdjustments(quantity);
        }
    }
}
