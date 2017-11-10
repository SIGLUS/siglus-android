package org.openlmis.core.utils.mapper;

import com.google.inject.Inject;

import org.openlmis.core.model.PTVProgram;
import org.openlmis.core.model.PTVProgramStockInformation;
import org.openlmis.core.model.ServiceDispensation;
import org.openlmis.core.view.viewmodel.ptv.PTVViewModel;

import java.util.List;

import lombok.Setter;

import static org.openlmis.core.utils.Constants.PTV_PRODUCT_FIFTH_CODE;
import static org.openlmis.core.utils.Constants.PTV_PRODUCT_FIRST_CODE;
import static org.openlmis.core.utils.Constants.PTV_PRODUCT_FOURTH_CODE;
import static org.openlmis.core.utils.Constants.PTV_PRODUCT_SECOND_CODE;
import static org.openlmis.core.utils.Constants.PTV_PRODUCT_THIRD_CODE;

public class PTVViewModelToPTVProgramMapper {

    @Setter
    private PTVProgram ptvProgram;

    @Inject
    public PTVViewModelToPTVProgramMapper(PTVProgram ptvProgram) {
        this.ptvProgram = ptvProgram;
    }

    public PTVProgram convertToPTVProgram(List<PTVViewModel> viewModels) {
        for (PTVProgramStockInformation ptvProgramStockInformation : ptvProgram.getPtvProgramStocksInformation()) {
            for (ServiceDispensation service : ptvProgramStockInformation.getServiceDispensations()) {
                String serviceName = service.getHealthFacilityService().getName();
                for (PTVViewModel ptvViewModel : viewModels) {
                    if(serviceName.equals(ptvViewModel.getPlaceholderItemName())){
                        switch (ptvProgramStockInformation.getProduct().getCode()){
                            case PTV_PRODUCT_FIRST_CODE:
                                service.setQuantity(ptvViewModel.getQuantity1());
                                break;
                            case PTV_PRODUCT_SECOND_CODE:
                                service.setQuantity(ptvViewModel.getQuantity2());
                                break;
                            case PTV_PRODUCT_THIRD_CODE:
                                service.setQuantity(ptvViewModel.getQuantity3());
                                break;
                            case PTV_PRODUCT_FOURTH_CODE:
                                service.setQuantity(ptvViewModel.getQuantity4());
                                break;
                            case PTV_PRODUCT_FIFTH_CODE:
                                service.setQuantity(ptvViewModel.getQuantity5());
                                break;
                        }

                    }
                }

            }
        }
        return ptvProgram;
    }
}
