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
import org.openlmis.core.utils.PTVUtil;
import org.openlmis.core.view.viewmodel.ptv.PTVViewModel;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(LMISTestRunner.class)
public class PTVViewModelToPTVProgramMapperTest {


    private PTVViewModelToPTVProgramMapper ptvViewModelToPTVProgramMapper;

    private List<PTVViewModel> ptvViewModels;

    private long[] quantities;

    @Before
    public void init() {
        ptvViewModelToPTVProgramMapper = new PTVViewModelToPTVProgramMapper();
        quantities = PTVUtil.getRandomQuantitiesForPTVViewModels();
        ptvViewModels = PTVUtil.getPtvViewModels(quantities);
    }

    @Test
    public void shouldFillPtvProgramListsFromViewModelList() {

        PTVProgram ptvProgram = createPtvProgram();

        PTVProgram resultPtvProgram = ptvViewModelToPTVProgramMapper.convertToPTVProgram(ptvViewModels, ptvProgram);

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
            if (serviceDispensation.getHealthFacilityService().getName().equals(PTVUtil.SERVICE_NAME)) {
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

}