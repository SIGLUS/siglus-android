package org.openlmis.core.model.repository;

import android.support.annotation.NonNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Cmm;
import org.openlmis.core.model.StockCard;
import org.robolectric.RuntimeEnvironment;

import java.util.Date;

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
        Cmm cmm = createDummyCmm();

        assertThat(cmmRepository.list().size(), is(0));

        //when
        cmmRepository.save(cmm);

        //then
        assertThat(cmmRepository.list().size(), is(1));
    }

    private Cmm createDummyCmm() {
        StockCard stockCard = new StockCard();

        Cmm cmm = new Cmm();
        cmm.setStockCard(stockCard);
        cmm.setCmmValue(123);
        cmm.setPeriodBegin(new Date());
        cmm.setPeriodEnd(new Date());
        cmm.setSynced(false);
        return cmm;
    }
}