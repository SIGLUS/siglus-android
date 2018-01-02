package org.openlmis.core.utils.mapper;

import android.support.annotation.NonNull;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.PTVProgram;
import org.openlmis.core.model.PTVProgramStockInformation;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.ServiceDispensation;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.PTVUtil;
import org.openlmis.core.view.viewmodel.ptv.PTVViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(LMISTestRunner.class)
public class PTVViewModelToPTVProgramMapperTest {

    private long[] quantities;
    private final String SERVICE_NAME = "Maternity";

    @Before
    public void init(){
        quantities = new long[5];
        for (int i = 0; i < 5; i++) {
            quantities[i] = 1 + new Random().nextInt(9);
        }
    }

    @Test
    public void shouldFillPtvProgramListsFromViewModelList() {
        List<PTVViewModel> ptvViewModels = getPtvViewModels();
        PTVProgram ptvProgram = createPtvProgram();
        PTVViewModelToPTVProgramMapper ptvViewModelToPTVProgramMapper = new PTVViewModelToPTVProgramMapper(ptvProgram);

        PTVProgram resultPtvProgram = ptvViewModelToPTVProgramMapper.convertToPTVProgram(ptvViewModels);

        assertPtvProgramStockInformationIsCorrectlyMapped(resultPtvProgram);
    }

    private void assertPtvProgramStockInformationIsCorrectlyMapped(PTVProgram resultPtvProgram) {
        List<PTVProgramStockInformation> ptvProgramStocksInformation = new ArrayList<>(resultPtvProgram.getPtvProgramStocksInformation());
        for (int index = 0; index < ptvProgramStocksInformation.size(); index++) {
            PTVProgramStockInformation ptvProgramStockInformation = ptvProgramStocksInformation.get(index);
            List<ServiceDispensation> serviceDispensations = new ArrayList<>(ptvProgramStockInformation.getServiceDispensations());
            assertServiceQuantities(index, serviceDispensations);
            assertThat(ptvProgramStockInformation.getLossesAndAdjustments(), is(quantities[index]));
            assertThat(ptvProgramStockInformation.getRequisition(), is(quantities[index]));
        }
    }

    private void assertServiceQuantities(int i, List<ServiceDispensation> serviceDispensations) {
        for (ServiceDispensation serviceDispensation : serviceDispensations) {
            if (serviceDispensation.getHealthFacilityService().getName().equals(SERVICE_NAME)) {
                assertThat(serviceDispensation.getQuantity(), is(quantities[i]));
            }
        }
    }

    @NonNull
    private PTVProgram createPtvProgram() {
        Period period = new Period(DateTime.now());
        PTVProgram ptvProgram = PTVUtil.createDummyPTVProgram(period);
        ptvProgram.setPtvProgramStocksInformation(PTVUtil.createDummyPTVProgramStocksInformation());
        return ptvProgram;
    }

    @NonNull
    private List<PTVViewModel> getPtvViewModels() {
        List<PTVViewModel> ptvViewModels = new ArrayList<>();
        ptvViewModels.add(createPTVViewModel(SERVICE_NAME));
        ptvViewModels.add(createPTVViewModel(Constants.REQUISITIONS));
        ptvViewModels.add(createPTVViewModel(Constants.LOSSES_AND_ADJUSTMENTS));
        return ptvViewModels;
    }

    private PTVViewModel createPTVViewModel(String name) {
        PTVViewModel ptvViewModel = new PTVViewModel(name);
        ptvViewModel.setQuantity(1, quantities[0]);
        ptvViewModel.setQuantity(2, quantities[1]);
        ptvViewModel.setQuantity(3, quantities[2]);
        ptvViewModel.setQuantity(4, quantities[3]);
        ptvViewModel.setQuantity(5, quantities[4]);
        return ptvViewModel;
    }
}