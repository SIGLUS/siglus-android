package org.openlmis.core.view.adapter;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.PTVProgram;
import org.openlmis.core.model.Period;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.PTVUtil;
import org.openlmis.core.view.viewmodel.ptv.PTVViewModel;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

@RunWith(LMISTestRunner.class)
public class PTVProgramAdapterTest {


    @Test
    public void shouldCalculateTotalCorrectlyForViewModels() {

        Period period = new Period(DateTime.now());
        PTVProgram ptvProgram = PTVUtil.createDummyPTVProgram(period);
        ptvProgram.setPtvProgramStocksInformation(PTVUtil.createDummyPTVProgramStocksInformation());
        long[] quantitiesForPTVViewModels = PTVUtil.getRandomQuantitiesForPTVViewModels();
        List<PTVViewModel> ptvViewModels = PTVUtil.getPtvViewModels(quantitiesForPTVViewModels);
        PTVViewModel ptvViewModelTotal = new PTVViewModel(Constants.TOTAL);
        PTVViewModel ptvViewModelFinalStock = new PTVViewModel(Constants.FINAL_STOCK);
        ptvViewModels.add(ptvViewModelTotal);
        ptvViewModels.add(ptvViewModelFinalStock);

        new PTVProgramAdapter(ptvProgram, ptvViewModels);

        assertTotalsAreCorrectlyCalculated(quantitiesForPTVViewModels, ptvViewModels);
    }

    private void assertTotalsAreCorrectlyCalculated(long[] quantitiesForPTVViewModels, List<PTVViewModel> ptvViewModels) {
        for (PTVViewModel model : ptvViewModels) {
            if (model.getPlaceholderItemName().equals(Constants.TOTAL)) {
                int FIRST_PRODUCT_INDEX = 0;
                int SECOND_PRODUCT_INDEX = 1;
                int THIRD_PRODUCT_INDEX = 2;
                int FOURTH_PRODUCT_INDEX = 3;
                int FIFTH_PRODUCT_INDEX = 4;
                assertThat(model.getQuantity1(), is(getTotalForProduct(quantitiesForPTVViewModels[FIRST_PRODUCT_INDEX])));
                assertThat(model.getQuantity2(), is(getTotalForProduct(quantitiesForPTVViewModels[SECOND_PRODUCT_INDEX])));
                assertThat(model.getQuantity3(), is(getTotalForProduct(quantitiesForPTVViewModels[THIRD_PRODUCT_INDEX])));
                assertThat(model.getQuantity4(), is(getTotalForProduct(quantitiesForPTVViewModels[FOURTH_PRODUCT_INDEX])));
                assertThat(model.getQuantity5(), is(getTotalForProduct(quantitiesForPTVViewModels[FIFTH_PRODUCT_INDEX])));
            }
        }
    }

    private long getTotalForProduct(long quantitiesForPTVViewModel) {
        return PTVProgramAdapter.TOTAL_SERVICES_NUMBER * quantitiesForPTVViewModel;
    }


}