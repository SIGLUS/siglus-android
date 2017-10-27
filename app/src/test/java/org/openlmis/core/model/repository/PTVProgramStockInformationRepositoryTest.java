package org.openlmis.core.model.repository;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.PTVProgram;
import org.openlmis.core.model.PTVProgramStockInformation;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.Product;
import org.openlmis.core.utils.PTVUtil;
import org.robolectric.RuntimeEnvironment;

import java.util.List;

import roboguice.RoboGuice;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

@RunWith(LMISTestRunner.class)
public class PTVProgramStockInformationRepositoryTest {

    private PTVProgramStockInformationRepository ptvProgramStockInformationRepository;
    private PTVProgram ptvProgram;
    private Product product;

    @Before
    public void setUp() throws Exception {
        ptvProgramStockInformationRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(PTVProgramStockInformationRepository.class);
        Period period = new Period(DateTime.now());
        ptvProgram = PTVUtil.createDummyPTVProgram(period);
        product = Product.dummyProduct();
    }

    @Test
    public void shouldSavePTVProgramStocksInformationWhenPTVProgramAndProductExist() throws LMISException {
        ptvProgram.setId(10L);
        product.setId(2L);
        PTVProgramStockInformation ptvProgramStockInformation = new PTVProgramStockInformation();
        ptvProgramStockInformation.setProduct(product);
        ptvProgramStockInformation.setPtvProgram(ptvProgram);
        PTVProgramStockInformation ptvProgramStockInformation2 = new PTVProgramStockInformation();
        ptvProgramStockInformation2.setProduct(product);
        ptvProgramStockInformation2.setPtvProgram(ptvProgram);
        List<PTVProgramStockInformation> ptvProgramStocksInformation = newArrayList(ptvProgramStockInformation, ptvProgramStockInformation2);

        boolean isPTVProgramStockInformationSaved = ptvProgramStockInformationRepository.save(ptvProgramStocksInformation);

        assertThat(isPTVProgramStockInformationSaved, is(true));
    }

    @Test (expected = LMISException.class)
    public void shouldThrowExceptionSavingPTVProgramStocksInformationWhenPTVProgramDoNotExist() throws LMISException {
        PTVProgramStockInformation ptvProgramStockInformation = new PTVProgramStockInformation();
        PTVProgramStockInformation ptvProgramStockInformation2 = new PTVProgramStockInformation();

        List<PTVProgramStockInformation> ptvProgramStocksInformation = newArrayList(ptvProgramStockInformation, ptvProgramStockInformation2);

        ptvProgramStockInformationRepository.save(ptvProgramStocksInformation);
    }
}