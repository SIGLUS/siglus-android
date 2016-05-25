package org.openlmis.core.model.repository;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Cmm;
import org.openlmis.core.model.StockCard;
import org.robolectric.RuntimeEnvironment;

import roboguice.RoboGuice;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

@RunWith(LMISTestRunner.class)
public class CmmRepositoryTest {

    private CmmRepository cmmRepository;

    @Before
    public void setup() throws LMISException {
        cmmRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(CmmRepository.class);
    }

    @Test
    public void shouldSaveCmm() throws Exception {
        //given
        Cmm cmm = createDummyCmm("2016-01-21", "2016-02-20", 7788, 123);

        assertThat(cmmRepository.list().size(), is(0));

        //when
        cmmRepository.save(cmm);

        //then
        assertThat(cmmRepository.list().size(), is(1));
    }

    @Test
    public void shouldUpdateInsteadOfCreateCmmEntryForSameStockCardInSamePeriod() throws LMISException {
        //given
        Cmm cmm = createDummyCmm("2016-01-21", "2016-02-20", 7788, 123);

        assertThat(cmmRepository.list().size(), is(0));

        //when
        cmmRepository.save(cmm);

        //then
        assertThat(cmmRepository.list().size(), is(1));
        assertThat(cmmRepository.list().get(0).getCmmValue(), is(123f));

        //when
        Cmm cmmOfSameCardSamePeriod = createDummyCmm("2016-01-21", "2016-02-20", 7788, 456);
        cmmRepository.save(cmmOfSameCardSamePeriod);

        //then
        assertThat(cmmRepository.list().size(), is(1));
        assertThat(cmmRepository.list().get(0).getCmmValue(), is(456f));
    }

    private Cmm createDummyCmm(String start, String end, int cardId, int cmmValue) {
        StockCard stockCard = new StockCard();
        stockCard.setId(cardId);

        Cmm cmm = new Cmm();
        cmm.setStockCard(stockCard);
        cmm.setCmmValue(cmmValue);
        cmm.setPeriodBegin(DateTime.parse(start).toDate());
        cmm.setPeriodEnd(DateTime.parse(end).toDate());
        cmm.setSynced(false);
        return cmm;
    }
}