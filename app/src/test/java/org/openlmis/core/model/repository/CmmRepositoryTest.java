package org.openlmis.core.model.repository;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.List;
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

@RunWith(LMISTestRunner.class)
public class CmmRepositoryTest {

  private CmmRepository cmmRepository;

  @Before
  public void setup() throws LMISException {
    cmmRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(CmmRepository.class);
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
  public void shouldUpdateInsteadOfCreateCmmEntryForSameStockCardInSamePeriod()
      throws LMISException {
    //given
    Cmm cmm = createDummyCmm("2016-01-21", "2016-02-20", 7788, 123);

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

  @Test
  public void shouldSaveSameCardDifferentPeriodCmmsAsSeparateEntries() throws LMISException {
    //given
    Cmm cmm = createDummyCmm("2016-01-21", "2016-02-20", 7788, 123);

    //when
    cmmRepository.save(cmm);

    //then
    assertThat(cmmRepository.list().size(), is(1));
    assertThat(cmmRepository.list().get(0).getCmmValue(), is(123f));

    //when
    Cmm cmmOfSameCardSamePeriod = createDummyCmm("2016-02-21", "2016-03-20", 7788, 456);
    cmmRepository.save(cmmOfSameCardSamePeriod);

    //then
    assertThat(cmmRepository.list().size(), is(2));
    assertThat(cmmRepository.list().get(1).getCmmValue(), is(456f));
  }

  @Test
  public void shouldListUnsyncedCmms() throws Exception {
    //given
    Cmm cmm1 = createDummyCmm("2016-01-21", "2016-02-20", 7788, 123);
    Cmm cmm2 = createDummyCmm("2016-01-21", "2016-02-20", 8899, 123);
    cmm1.setSynced(true);
    cmm2.setSynced(false);

    cmmRepository.save(cmm1);
    cmmRepository.save(cmm2);

    //when
    List<Cmm> allCmms = cmmRepository.list();
    List<Cmm> unsyncedCmms = cmmRepository.listUnsynced();

    //then
    assertThat(unsyncedCmms.size(), is(1));
    assertThat(allCmms.size(), is(2));
  }

  private Cmm createDummyCmm(String start, String end, int cardId, int cmmValue) {
    StockCard stockCard = new StockCard();
    stockCard.setId(cardId);

    Cmm cmm = new Cmm();
    cmm.setStockCard(stockCard);
    cmm.setCmmValue(cmmValue);
    cmm.setPeriodBegin(DateTime.parse(start).toDate());
    cmm.setPeriodEnd(DateTime.parse(end).toDate());
    return cmm;
  }
}